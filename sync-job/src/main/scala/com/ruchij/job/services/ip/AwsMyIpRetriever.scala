package com.ruchij.job.services.ip

import java.net.InetAddress

import cats.effect.Sync
import cats.implicits._
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

class AwsMyIpRetriever[F[_]: Sync](client: Client[F]) extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    client.expect[String](AwsMyIpRetriever.serverUrl)
      .flatMap { response => Sync[F].delay(InetAddress.getByName(response.trim)) }

}

object AwsMyIpRetriever {
  val serverUrl = uri"https://checkip.amazonaws.com"
}
