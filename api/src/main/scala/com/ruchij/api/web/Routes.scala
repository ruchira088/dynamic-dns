package com.ruchij.api.web

import cats.ApplicativeError
import cats.effect.Sync
import cats.implicits.toFlatMapOps
import com.ruchij.api.exceptions.RemoteIpNotDeterminableException
import com.ruchij.core.circe.Encoders.dateTimeEncoder
import com.ruchij.core.responses.IpAddressResponse
import com.ruchij.core.responses.IpAddressResponse.inetSocketAddressEncoder
import com.ruchij.api.services.health.HealthService
import com.ruchij.api.web.middleware.{ExceptionHandler, NotFoundHandler}
import io.circe.generic.auto.exportEncoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpApp, HttpRoutes, Response}

object Routes {
  def apply[F[_]: Sync](healthService: HealthService[F]): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    import dsl._

    val routes: HttpRoutes[F] =
      HttpRoutes.of {
        case GET -> Root / "health" =>
          healthService
            .serviceInformation()
            .flatMap(serviceInformation => Ok(serviceInformation))

        case request @ GET -> Root =>
          request.remote
            .fold[F[Response[F]]](ApplicativeError[F, Throwable].raiseError(RemoteIpNotDeterminableException)) {
              inetSocketAddress => Ok(IpAddressResponse(inetSocketAddress))
            }
      }

    ExceptionHandler {
      NotFoundHandler(routes)
    }
  }
}
