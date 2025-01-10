resource "aws_iam_user" "dynamic_dns_sync_job" {
  name = "dynamic-dns-sync-job"
}

resource "aws_iam_user_policy" "dynamic_dns_sync_job_iam_policy" {
  name = "dynamic-dns-sync-job-iam-policy"
  user = aws_iam_user.dynamic_dns_sync_job.name

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "route53:ChangeResourceRecordSets"
        ],
        Resource = "arn:aws:route53:::hostedzone/*"
      },
      {
        Effect = "Allow",
        Action = [
          "route53:ListHostedZones"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "sns:Publish"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_access_key" "dynamic_dns_sync_job_access_key" {
  user = aws_iam_user.dynamic_dns_sync_job.name
}

resource "aws_ssm_parameter" "dynamic_dns_sync_job_access_key_id" {
  name = "/dynamic-dns/sync-job/aws-access-key-id"
  type = "SecureString"
  value = aws_iam_access_key.dynamic_dns_sync_job_access_key.id
}

resource "aws_ssm_parameter" "dynamic_dns_sync_job_secret_access_key" {
  name = "/dynamic-dns/sync-job/aws-secret-access-key"
  type = "SecureString"
  value = aws_iam_access_key.dynamic_dns_sync_job_access_key.secret
}