package com.ruchij.serverless.handlers

import cats.effect.{Clock, IO}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import com.ruchij.serverless.Transformers
import org.http4s.Request

class ApiGatewayRequestHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  override def handleRequest(
    apiGatewayProxyRequestEvent: APIGatewayProxyRequestEvent,
    context: Context
  ): APIGatewayProxyResponseEvent = {
    val http4sRequest: IO[Request[IO]] = Transformers.requestDecoder[IO](apiGatewayProxyRequestEvent)
    implicit val clock: Clock[IO] = Clock.create[IO]

    val healthService = new HealthServiceImpl[IO]

    val result =
      for {
        request <- http4sRequest
        http4sResponse <- Routes(healthService).run(request)

        response <- Transformers.responseEncoder(http4sResponse)
      } yield response

    result.unsafeRunSync()
  }

}
