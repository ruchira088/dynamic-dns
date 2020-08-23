package com.ruchij.job

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits._
import cats.{Applicative, Monad}
import com.ruchij.job.config.{DnsConfiguration, JobConfiguration}
import com.ruchij.job.models.JobResult
import com.ruchij.job.services.dns.{AwsRoute53Service, DnsManagementService}
import com.ruchij.job.services.hostname.{HostnameResolver, LocalHostnameResolver}
import com.ruchij.job.services.ip.{ApiMyIpRetriever, AwsMyIpRetriever, MyIpRetriever}
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.route53.Route53AsyncClient

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object JobApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      jobConfiguration <- JobConfiguration.parse[IO](configObjectSource)

      result <- application[IO](jobConfiguration)
      _ <- IO.delay(println(result.message))

    } yield ExitCode.Success

  def application[F[_]: ConcurrentEffect](jobConfiguration: JobConfiguration): F[JobResult] =
    BlazeClientBuilder[F](ExecutionContext.global).resource.use { httpClient =>
      val hostnameResolver: LocalHostnameResolver[F] = new LocalHostnameResolver[F]
      val apiMyIpRetriever: ApiMyIpRetriever[F] = new ApiMyIpRetriever[F](httpClient, jobConfiguration.apiServer)
      val awsMyIpRetriever: AwsMyIpRetriever[F] = new AwsMyIpRetriever[F](httpClient)

      val route53AsyncClient: Route53AsyncClient = Route53AsyncClient.builder().region(Region.AWS_GLOBAL).build()
      val dnsManagementService = new AwsRoute53Service[F](route53AsyncClient)

      execute[F](hostnameResolver, awsMyIpRetriever, dnsManagementService, jobConfiguration.dns)
    }

  def execute[F[_]: Monad](
    hostnameResolver: HostnameResolver[F],
    myIpRetriever: MyIpRetriever[F],
    dnsManagementService: DnsManagementService[F],
    dnsConfiguration: DnsConfiguration
  ): F[JobResult] =
    for {
      myIp <- myIpRetriever.ip
      dnsHostIp <- hostnameResolver.ipAddress(dnsConfiguration.host)

      result <- if (dnsHostIp.forall(_.getHostAddress != myIp.getHostAddress)) {
        dnsManagementService.upsert(dnsConfiguration.host, myIp)
      } else Applicative[F].pure(JobResult.NoChange(dnsConfiguration.host, myIp))

    } yield result

}
