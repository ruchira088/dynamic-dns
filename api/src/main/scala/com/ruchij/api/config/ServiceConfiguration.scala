package com.ruchij.api.config

import cats.ApplicativeError
import com.ruchij.core.types.FunctionKTypes.eitherToF
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

case class ServiceConfiguration(httpConfiguration: HttpConfiguration)

object ServiceConfiguration {
  def parse[F[_]: ApplicativeError[*[_], Throwable]](configObjectSource: ConfigObjectSource): F[ServiceConfiguration] =
    eitherToF.apply {
      configObjectSource.load[ServiceConfiguration].left.map(ConfigReaderException.apply)
    }
}