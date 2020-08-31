package com.ruchij.job.services.ip

import java.net.InetAddress

import cats.{Applicative, ApplicativeError}
import cats.implicits._
import cats.effect.Concurrent
import com.ruchij.job.exceptions.MyIpHostAddressMismatchException

class ConsolidatedMyIpRetriever[F[_]: Concurrent](
  apiMyIpRetriever: ApiMyIpRetriever[F],
  awsMyIpRetriever: AwsMyIpRetriever[F]
) extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    for {
      apiFiber <- Concurrent[F].start(apiMyIpRetriever.ip)

      ipFromAws <- awsMyIpRetriever.ip
      ipFromApi <- apiFiber.join

      _ <-
        if (ipFromAws.getHostAddress != ipFromApi.getHostAddress)
          ApplicativeError[F, Throwable].raiseError(MyIpHostAddressMismatchException(ipFromApi, ipFromAws))
        else Applicative[F].unit
    }
    yield ipFromApi

}
