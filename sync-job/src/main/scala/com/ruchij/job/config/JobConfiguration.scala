package com.ruchij.job.config

import cats.ApplicativeError
import com.ruchij.core.types.FunctionKTypes
import com.ruchij.job.config.JobConfigReaders.phoneNumberConfigReader
import org.http4s.Uri
import pureconfig.{ConfigObjectSource, ConfigReader}
import pureconfig.error.{CannotConvert, ConfigReaderException, FailureReason}
import pureconfig.generic.auto._

case class JobConfiguration(
  dns: DnsConfiguration,
  apiServer: ApiServerConfiguration,
  cloudflareApi: CloudflareApiConfiguration,
  notification: NotificationConfig,
)

object JobConfiguration {
  implicit val uriConfigReader: ConfigReader[Uri] =
    ConfigReader[String].emap { value =>
      Uri
        .fromString(value)
        .fold[Either[FailureReason, Uri]](
          throwable => Left(CannotConvert(value, classOf[Uri].getSimpleName, throwable.details)),
          Right.apply
        )
    }

  def parse[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[JobConfiguration] =
    FunctionKTypes.eitherToF.apply {
      configObjectSource.load[JobConfiguration].left.map(ConfigReaderException.apply)
    }
}
