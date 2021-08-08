package com.ruchij.job.services.ip

import cats.effect.Sync
import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

import java.net.InetAddress

class AwsMyIpRetriever[F[_]: Async](client: Client[F]) extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    client.expect[String](AwsMyIpRetriever.serverUrl)
      .flatMap { response => Sync[F].delay(InetAddress.getByName(response.trim)) }

}

object AwsMyIpRetriever {
  val serverUrl = uri"https://checkip.amazonaws.com"
}
