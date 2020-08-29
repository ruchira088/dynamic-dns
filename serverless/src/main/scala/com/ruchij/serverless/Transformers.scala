package com.ruchij.serverless

import java.net.InetSocketAddress

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.ruchij.core.types.FunctionKTypes.eitherToF
import fs2.Stream
import io.chrisdavenport.vault.Vault
import org.http4s.Request.Connection
import org.http4s._

import scala.jdk.CollectionConverters._

object Transformers {

  def requestDecoder[F[_]: Sync](apiGatewayProxyRequestEvent: APIGatewayProxyRequestEvent): F[Request[F]] =
    Sync[F]
      .delay(Option(apiGatewayProxyRequestEvent.getBody))
      .map(_.fold[Stream[F, Byte]](Stream.empty)(body => Stream.emits[F, Byte](body.getBytes)))
      .flatMap { body =>
        for {
          httpMethod <- eitherToF[Throwable, F].apply {
            Method.fromString(Option(apiGatewayProxyRequestEvent.getHttpMethod).getOrElse(Method.GET.name))
          }

          queryParams = Option(apiGatewayProxyRequestEvent.getMultiValueQueryStringParameters)
            .map {
              _.asScala.toList
                .flatMap {
                  case (key, values) =>
                    values.asScala.toSeq.map(value => s"$key=$value")
                }
                .mkString("?", "&", "")
            }
            .getOrElse("")

          pathUrl = Option(apiGatewayProxyRequestEvent.getPath).getOrElse("/")
          uri <- eitherToF[Throwable, F].apply(Uri.fromString(pathUrl + queryParams))

          headers = Option(apiGatewayProxyRequestEvent.getHeaders)
            .map {
              _.asScala.toMap.map {
                case (key, value) => Header(key, value)
              }
            }
            .map(headers => Headers(headers.toList))
            .getOrElse(Headers.empty)

          attributes <- Option(apiGatewayProxyRequestEvent.getRequestContext)
            .flatMap(requestContext => Option(requestContext.getIdentity))
            .flatMap(identity => Option(identity.getSourceIp))
            .fold(Applicative[F].pure(Vault.empty)) { sourceIp =>
              Sync[F]
                .delay(new InetSocketAddress(sourceIp, 443))
                .map { inetSocketAddress =>
                  Vault.empty.insert(
                    Request.Keys.ConnectionInfo,
                    Connection(inetSocketAddress, inetSocketAddress, secure = true)
                  )
                }
            }
        } yield Request[F](method = httpMethod, uri = uri, headers = headers, body = body, attributes = attributes)
      }

  def responseEncoder[F[_]: Sync](response: Response[F]): F[APIGatewayProxyResponseEvent] =
    for {
      bytes <- response.body.compile.toList
      body = new String(bytes.toArray)

      headers = response.headers.iterator
        .map(header => header.name.value -> header.value)
        .toMap
        .asJava

    } yield new APIGatewayProxyResponseEvent().withBody(body).withHeaders(headers).withStatusCode(response.status.code)

}
