package com.ruchij.core.responses

import java.net.InetSocketAddress

import io.circe.Encoder

case class IpAddressResponse(ip: InetSocketAddress)

object IpAddressResponse {
  implicit val inetSocketAddressEncoder: Encoder[InetSocketAddress] =
    Encoder[String].contramap[InetSocketAddress] {
      inetSocketAddress =>
        Option(inetSocketAddress.getAddress).map(_.getHostAddress)
          .getOrElse(inetSocketAddress.getHostName)
    }
}