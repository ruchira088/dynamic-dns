# Dynamic DNS

A Scala-based system that automatically keeps DNS records synchronized with the current public IP address. It provides an IP address lookup API deployed as AWS Lambda functions, and a sync job that periodically updates Route53 DNS records.

## Architecture

The project is a multi-module sbt build consisting of four modules:

```
dynamic-dns/
├── core/           Shared models, configuration, and utilities
├── api/            HTTP API service (http4s)
├── serverless/     AWS Lambda adapter for the API
└── sync-job/       Scheduled DNS synchronization job
```

### Core

Shared library containing HTTP4s + Circe JSON integration, configuration readers, Joda Time utilities, and response models used across modules.

### API

REST API built with [http4s](https://http4s.org/) and Ember server:

| Endpoint     | Description                                  |
|--------------|----------------------------------------------|
| `GET /`      | Returns the caller's public IP address       |
| `GET /health`| Returns service and build information         |

Configurable via `application.conf`:

```hocon
http-configuration {
  host = "0.0.0.0"    # env: HTTP_HOST
  port = 8000         # env: HTTP_PORT
}
```

### Serverless

AWS Lambda wrapper that transforms API Gateway proxy events to/from http4s requests, allowing the same routing logic to run on Lambda. Deployed via AWS SAM to `ip.ruchij.com`.

### Sync Job

Scheduled job that:

1. Retrieves the current public IP from multiple sources (Dynamic DNS API, Cloudflare Worker, AWS checkip) with redundancy validation
2. Resolves the current DNS record for the configured hostname
3. Updates the Route53 A record if the IP has changed
4. Optionally sends an SMS notification via AWS SNS

Configurable via `application.conf`:

```hocon
dns.host = "home.ruchij.com"                         # env: DNS_HOST
api-server.url = "https://ip.ruchij.com"              # env: API_URL
cloudflare-api.url = "https://ip.ruchij.workers.dev"  # env: CLOUDFLARE_API_URL
notification.alert-sms-phone-number = null            # env: ALERT_SMS_PHONE_NUMBER
```

## Tech Stack

| Category        | Technology                                       |
|-----------------|--------------------------------------------------|
| Language        | Scala 2.13.18                                    |
| Build           | sbt 1.12.8                                       |
| Runtime         | Java 25                                          |
| HTTP            | http4s 0.23.33 (Ember)                           |
| JSON            | Circe 0.14.15                                    |
| AWS             | AWS SDK v2 42.22 (Route53, SNS)                  |
| Configuration   | PureConfig 0.17.10                               |
| Logging         | Logback 1.5.32, Logstash Encoder 9.0             |
| Testing         | ScalaTest 3.2.20                                 |
| Lambda          | AWS Lambda Java Core 1.4.0, Events 3.16.1        |

## Getting Started

### Prerequisites

- JDK 25
- sbt 1.12.8+
- AWS CLI (for deployment)
- Docker (for K8s deployment)

### Build

```bash
# Compile all modules
sbt compile

# Run tests
sbt test

# Build the Lambda fat JAR
sbt serverless/assembly

# Package API and sync-job for Docker
sbt api/Universal/packageZipTarball
sbt syncJob/Universal/packageZipTarball
```

### Run Locally

```bash
# Start the API server (defaults to 0.0.0.0:8000)
sbt api/run

# Run the sync job
sbt syncJob/run
```

## Deployment

### CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/build-pipeline.yml`) runs on every push and executes:

1. **compile-and-test** - Compiles and runs the test suite
2. **publish-docker-images** - Builds multi-arch Docker images and pushes to GHCR
3. **create-terraform-resources** - Applies Terraform for IAM and SSM resources
4. **deploy-sam** - Deploys Lambda functions via AWS SAM
5. **deploy-to-k8s** - Deploys API and sync job to Kubernetes
6. **send-notification** - Posts pipeline result to Slack

AWS authentication uses OIDC with an IAM role.

### AWS SAM (Lambda)

The serverless module is deployed as two Lambda functions behind API Gateway with a custom domain (`ip.ruchij.com`).

```bash
cd serverless

# Build the fat JAR
cd .. && sbt clean serverless/assembly && cd serverless

# Deploy
sam deploy
```

Configuration is in `serverless/samconfig.toml`. The SAM template (`serverless/template.yaml`) provisions:

- Two Lambda functions (health check and IP address)
- API Gateway (regional endpoint)
- ACM certificate with DNS validation
- Custom domain with Route53 A and AAAA records

### Terraform

Terraform manages IAM resources for the sync job:

- IAM user with Route53 and SNS permissions
- Access keys stored in AWS SSM Parameter Store

```bash
cd terraform
terraform init
terraform apply
```

### Kubernetes

The API and sync job are deployed to Kubernetes via Ansible playbooks:

- **API**: Deployment + Service + Ingress at `ip.dev.ruchij.com`
- **Sync Job**: CronJob with ConfigMap and Secrets for AWS credentials

```bash
ansible-playbook playbooks/k8s-deploy.yml
```

### Docker

Docker images are based on `eclipse-temurin:25-jre` and published to GHCR:

```bash
ansible-playbook playbooks/build-and-publish-docker-images.yml
```

## Project Structure

```
dynamic-dns/
├── api/src/main/scala/com/ruchij/api/
│   ├── ApiApp.scala                    # API entry point
│   └── web/
│       ├── Routes.scala                # Endpoint definitions
│       └── middleware/ExceptionHandler.scala
├── core/src/main/scala/com/ruchij/core/
│   ├── config/                         # Configuration readers
│   └── types/                          # Custom types and functions
├── serverless/
│   ├── src/main/scala/com/ruchij/serverless/
│   │   ├── handlers/ApiGatewayRequestHandler.scala
│   │   └── Transformers.scala
│   ├── template.yaml                   # SAM template
│   └── samconfig.toml                  # SAM deployment config
├── sync-job/src/main/scala/com/ruchij/job/
│   ├── JobApp.scala                    # Sync job entry point
│   └── services/
│       ├── dns/AwsRoute53Service.scala
│       ├── ip/ConsolidatedMyIpRetriever.scala
│       └── notification/AmazonSnsNotificationService.scala
├── terraform/
│   ├── providers.tf
│   └── resources.tf
├── playbooks/
│   ├── sam-deploy.yml
│   ├── k8s-deploy.yml
│   ├── build-and-publish-docker-images.yml
│   ├── docker/                         # Dockerfile templates
│   └── k8s/                            # K8s manifest templates
├── .github/workflows/build-pipeline.yml
├── build.sbt
├── project/Dependencies.scala
└── version.sbt
```
