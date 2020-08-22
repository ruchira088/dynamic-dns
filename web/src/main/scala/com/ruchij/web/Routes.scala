package com.ruchij.web

import cats.effect.Sync
import cats.implicits.toFlatMapOps
import com.ruchij.circe.Encoders.dateTimeEncoder
import com.ruchij.models.RemoteAddress
import com.ruchij.responses.IpAddressResponse
import com.ruchij.services.health.HealthService
import com.ruchij.web.middleware.{ExceptionHandler, NotFoundHandler}
import io.circe.generic.auto.exportEncoder
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpApp, HttpRoutes}

object Routes {
  def apply[F[_]: Sync](healthService: HealthService[F]): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    import dsl._

    val routes: HttpRoutes[F] =
      HttpRoutes.of {
        case GET -> Root / "service" =>
          healthService.serviceInformation()
            .flatMap(serviceInformation => Ok(serviceInformation))

        case request @ GET -> Root / "ip" =>
          Ok {
            IpAddressResponse(RemoteAddress.parse(request))
          }
      }

    ExceptionHandler {
      NotFoundHandler(routes)
    }
  }
}
