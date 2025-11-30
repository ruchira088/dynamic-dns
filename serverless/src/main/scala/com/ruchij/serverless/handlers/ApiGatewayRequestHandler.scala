package com.ruchij.serverless.handlers

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import com.ruchij.serverless.Transformers

class ApiGatewayRequestHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  override def handleRequest(
    apiGatewayProxyRequestEvent: APIGatewayProxyRequestEvent,
    context: Context
  ): APIGatewayProxyResponseEvent = {
    val result =
      for {
        request <- Transformers.requestDecoder[IO](apiGatewayProxyRequestEvent)

        healthService = new HealthServiceImpl[IO]
        http4sResponse <- Routes(healthService).run(request)

        response <- Transformers.responseEncoder(http4sResponse)
      } yield response

    result.unsafeRunSync()
  }

}
