package com.ruchij.api

import cats.effect.{ExitCode, IO, IOApp}
import com.ruchij.api.config.ServiceConfiguration
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource

object ApiApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      healthService = new HealthServiceImpl[IO](serviceConfiguration.buildInformation)

      _ <-
        BlazeServerBuilder[IO]
          .withHttpApp(Routes(healthService))
          .bindHttp(serviceConfiguration.httpConfiguration.port, serviceConfiguration.httpConfiguration.host)
          .serve.compile.drain

    }
      yield ExitCode.Success
}
