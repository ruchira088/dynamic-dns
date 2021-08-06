package com.ruchij.serverless.handlers

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.ruchij.api.config.ServiceConfiguration
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import com.ruchij.serverless.Transformers
import pureconfig.ConfigSource

class ApiGatewayRequestHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  override def handleRequest(
    apiGatewayProxyRequestEvent: APIGatewayProxyRequestEvent,
    context: Context
  ): APIGatewayProxyResponseEvent = {
    val result =
      for {
        configObjectSource <- IO.delay(ConfigSource.defaultApplication)
        serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)
        healthService = new HealthServiceImpl[IO](serviceConfiguration.buildInformation)

        request <- Transformers.requestDecoder[IO](apiGatewayProxyRequestEvent)
        http4sResponse <- Routes(healthService).run(request)

        response <- Transformers.responseEncoder(http4sResponse)
      } yield response

    result.unsafeRunSync()
  }

}
