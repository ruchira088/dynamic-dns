package com.ruchij.api.test.utils

import cats.Applicative
import cats.effect.{Clock, Sync}
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

object Providers {
  implicit def clock[F[_]: Sync]: Clock[F] = stubClock(DateTime.now())

  implicit def stubClock[F[_]: Sync](dateTime: => DateTime): Clock[F] = new Clock[F] {
    override def applicative: Applicative[F] = Applicative[F]

    override def monotonic: F[FiniteDuration] = realTime

    override def realTime: F[FiniteDuration] = Sync[F].delay(FiniteDuration(dateTime.getMillis, TimeUnit.MILLISECONDS))
  }
}
