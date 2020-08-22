package com.ruchij.models

import java.net.InetAddress

import io.circe.Encoder
import org.http4s.Request

sealed trait RemoteAddress

object RemoteAddress {

  case object Unknown extends RemoteAddress

  case class IpAddress(inetAddress: InetAddress) extends RemoteAddress {
    override def toString: String = inetAddress.getHostAddress
  }

  def parse[F[_]](request: Request[F]): RemoteAddress =
    request.remote
      .map { inetSocketAddress => IpAddress(inetSocketAddress.getAddress) }
      .getOrElse(Unknown)

  implicit val circeEncoder: Encoder[RemoteAddress] = Encoder[String].contramap[RemoteAddress](_.toString)
}
