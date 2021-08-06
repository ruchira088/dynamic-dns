package com.ruchij.api.services.health

import cats.effect.Clock
import cats.implicits._
import cats.{Defer, Monad}
import com.ruchij.api.services.health.models.ServiceInformation
import com.ruchij.core.config.BuildInformation
import org.joda.time.DateTime

class HealthServiceImpl[F[_]: Defer: Clock: Monad](buildInformation: BuildInformation) extends HealthService[F] {
  override def serviceInformation(): F[ServiceInformation] =
    Clock[F].realTime
      .flatMap(duration => ServiceInformation.create[F](new DateTime(duration.toMillis), buildInformation))
}
