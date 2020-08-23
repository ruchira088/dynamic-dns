package com.ruchij.job.services.ip

import java.net.InetAddress

import cats.effect.Sync
import cats.implicits._
import com.ruchij.core.responses.IpAddressResponse
import com.ruchij.job.config.ApiServerConfiguration
import org.http4s.client.Client
import io.circe.generic.auto.exportDecoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

class ApiMyIpRetriever[F[_]: Sync](client: Client[F], apiServerConfiguration: ApiServerConfiguration)
    extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    client.expect[IpAddressResponse](apiServerConfiguration.url)
      .flatMap {
        case IpAddressResponse(ip) => Sync[F].delay(InetAddress.getByName(ip))
      }

}