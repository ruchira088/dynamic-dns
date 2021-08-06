package com.ruchij.job.services.ip

import cats.effect.Concurrent
import cats.implicits._
import cats.{Applicative, Defer}
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

import java.net.InetAddress

class AwsMyIpRetriever[F[_]: Defer: Concurrent](client: Client[F]) extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    client.expect[String](AwsMyIpRetriever.serverUrl)
      .flatMap { response => Defer[F].defer(Applicative[F].point(InetAddress.getByName(response.trim))) }

}

object AwsMyIpRetriever {
  val serverUrl = uri"https://checkip.amazonaws.com"
}
