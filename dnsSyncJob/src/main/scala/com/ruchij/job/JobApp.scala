package com.ruchij.job

import cats.{Applicative, Monad}
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits._
import com.ruchij.job.config.{DnsConfiguration, JobConfiguration}
import com.ruchij.job.models.JobResult
import com.ruchij.job.services.hostname.{HostnameResolver, LocalHostnameResolver}
import com.ruchij.job.services.ip.{ApiHostIpRetriever, HostIpRetriever}
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext

object JobApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      jobConfiguration <- JobConfiguration.parse[IO](configObjectSource)

      _ <- application[IO](jobConfiguration)
    } yield ExitCode.Success

  def application[F[_]: ConcurrentEffect](jobConfiguration: JobConfiguration): F[JobResult] =
    BlazeClientBuilder[F](ExecutionContext.global).resource.use { httpClient =>
      val hostnameResolver = new LocalHostnameResolver[F]
      val hostIpRetriever = new ApiHostIpRetriever[F](httpClient, jobConfiguration.apiServer)

      execute[F](hostnameResolver, hostIpRetriever, jobConfiguration.dns)
    }

  def execute[F[_]: Monad](
    hostnameResolver: HostnameResolver[F],
    hostIpRetriever: HostIpRetriever[F],
    dnsConfiguration: DnsConfiguration
  ): F[JobResult] =
    for {
      myIp <- hostIpRetriever.hostIp
      dnsHostIp <- hostnameResolver.ipAddress(dnsConfiguration.host)

      result <-
        if (dnsHostIp.forall(_.getHostAddress != myIp.getHostAddress)) {
          ???
        } else Applicative[F].pure(JobResult.NoChange)

    } yield result

}
