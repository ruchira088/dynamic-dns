package com.ruchij.job.services.ip

import cats.effect.Concurrent
import cats.implicits._
import cats.{Applicative, ApplicativeError}
import com.ruchij.core.types.FunctionKTypes.OutcomeOps
import com.ruchij.job.exceptions.MyIpHostAddressMismatchException

import java.net.InetAddress

class ConsolidatedMyIpRetriever[F[_]: Concurrent](
  myIpRetrieverOne: MyIpRetriever[F],
  myIpRetrieverTwo: MyIpRetriever[F]
) extends MyIpRetriever[F] {

  override val ip: F[InetAddress] =
    for {
      apiFiber <- Concurrent[F].start(myIpRetrieverOne.ip)

      ipFromAws <- myIpRetrieverTwo.ip
      ipFromApi <- apiFiber.join.flatMap(_.toF)

      _ <- if (ipFromAws.getHostAddress != ipFromApi.getHostAddress)
        ApplicativeError[F, Throwable].raiseError(MyIpHostAddressMismatchException(ipFromApi, ipFromAws))
      else Applicative[F].unit
    } yield ipFromApi

}

object ConsolidatedMyIpRetriever {
  def apply[F[_]: Concurrent](
    myIpRetrieverOne: MyIpRetriever[F],
    myIpRetrieverTwo: MyIpRetriever[F],
    myIpRetrievers: MyIpRetriever[F]*
  ): ConsolidatedMyIpRetriever[F] =
    myIpRetrievers.foldLeft(new ConsolidatedMyIpRetriever[F](myIpRetrieverOne, myIpRetrieverTwo)) {
      case (consolidatedMyIpRetriever, myIpRetriever) =>
        new ConsolidatedMyIpRetriever[F](consolidatedMyIpRetriever, myIpRetriever)
    }
}
