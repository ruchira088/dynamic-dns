package com.ruchij.job.services.hostname

import java.net.{InetAddress, UnknownHostException}

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, ApplicativeError}

class LocalHostnameResolver[F[_]: Sync] extends HostnameResolver[F] {

  override def ipAddress(hostname: String): F[Option[InetAddress]] = {
    Sync[F].handleErrorWith(Sync[F].delay(InetAddress.getByName(hostname)).map(Option.apply)) {

      case _: UnknownHostException => Applicative[F].pure(None)

      case throwable => ApplicativeError[F, Throwable].raiseError(throwable)
    }
  }

}
