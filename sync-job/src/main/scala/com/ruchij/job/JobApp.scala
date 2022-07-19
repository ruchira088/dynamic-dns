package com.ruchij.job

import cats.effect.kernel.Async
import cats.effect.{Concurrent, ExitCode, IO, IOApp}
import cats.implicits._
import cats.{Applicative, Monad}
import com.ruchij.core.config.BuildInformation
import com.ruchij.job.config.{DnsConfiguration, JobConfiguration, NotificationConfig}
import com.ruchij.job.models.JobResult
import com.ruchij.job.services.dns.{AwsRoute53Service, DnsManagementService}
import com.ruchij.job.services.hostname.{HostnameResolver, LocalHostnameResolver}
import com.ruchij.job.services.ip.{ApiMyIpRetriever, AwsMyIpRetriever, ConsolidatedMyIpRetriever, MyIpRetriever}
import com.ruchij.job.services.notification.sms.{AmazonSnsNotificationService, SmsNotificationService}
import org.http4s.ember.client.EmberClientBuilder
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext.Implicits.global

object JobApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      jobConfiguration <- JobConfiguration.parse[IO](configObjectSource)
      _ <- IO.delay(println(banner(jobConfiguration.buildInformation)))

      result <- application[IO](jobConfiguration)

      _ <- IO.delay(println(result.message))

    } yield ExitCode.Success

  def application[F[_]: Concurrent: Async](jobConfiguration: JobConfiguration): F[JobResult] =
    EmberClientBuilder.default[F].build.use { httpClient =>
      val hostnameResolver: LocalHostnameResolver[F] = new LocalHostnameResolver[F]

      val apiMyIpRetriever: ApiMyIpRetriever[F] = new ApiMyIpRetriever[F](httpClient, jobConfiguration.apiServer)
      val awsMyIpRetriever: AwsMyIpRetriever[F] = new AwsMyIpRetriever[F](httpClient)
      val myIpRetriever: ConsolidatedMyIpRetriever[F] = new ConsolidatedMyIpRetriever[F](apiMyIpRetriever, awsMyIpRetriever)

      for {
        dnsManagementService <- AwsRoute53Service.create[F]
        smsNotificationService <- AmazonSnsNotificationService.create[F]
        result <- execute[F](hostnameResolver, myIpRetriever, dnsManagementService, smsNotificationService, jobConfiguration.dns, jobConfiguration.notification)
      }
      yield result
    }

  def execute[F[_]: Monad](
    hostnameResolver: HostnameResolver[F],
    myIpRetriever: MyIpRetriever[F],
    dnsManagementService: DnsManagementService[F],
    smsNotificationService: SmsNotificationService[F],
    dnsConfiguration: DnsConfiguration,
    notificationConfig: NotificationConfig
  ): F[JobResult] =
    for {
      myIp <- myIpRetriever.ip
      dnsHostIp <- hostnameResolver.ipAddress(dnsConfiguration.host)

      result <- if (dnsHostIp.forall(_.getHostAddress != myIp.getHostAddress))
        dnsManagementService.upsert(dnsConfiguration.host, myIp)
          .productL {
            notificationConfig.alertSmsPhoneNumber.fold(Applicative[F].unit) { phoneNumber =>
              smsNotificationService.send(phoneNumber, s"${dnsConfiguration.host} has been updated to ${myIp.getHostAddress}")
                .productR(Applicative[F].unit)
            }
          }
      else Applicative[F].pure(JobResult.NoChange(dnsConfiguration.host, myIp))

    } yield result

  def banner(buildInformation: BuildInformation): String =
    raw"""
      |  ____   _   _  ____    ____                            _         _
      | |  _ \ | \ | |/ ___|  / ___|  _   _  _ __    ___      | |  ___  | |__
      | | | | ||  \| |\___ \  \___ \ | | | || '_ \  / __|  _  | | / _ \ | '_ \
      | | |_| || |\  | ___) |  ___) || |_| || | | || (__  | |_| || (_) || |_) |
      | |____/ |_| \_||____/  |____/  \__, ||_| |_| \___|  \___/  \___/ |_.__/
      |                               |___/
      | DNS Sync Job
      |   Git branch: ${buildInformation.gitBranch.getOrElse("")}
      |   Git commit: ${buildInformation.gitCommit.getOrElse("")}
      |   Build timestamp: ${buildInformation.buildTimestamp.map(_.toString).getOrElse("")}
      |""".stripMargin

}
