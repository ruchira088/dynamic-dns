package com.ruchij.api.services.health

import cats.effect.Clock
import cats.effect.kernel.Sync
import cats.implicits._
import com.ruchij.api.services.health.models.ServiceInformation
import com.ruchij.core.config.BuildInformation
import org.joda.time.DateTime

class HealthServiceImpl[F[_]: Sync](buildInformation: BuildInformation)(implicit clock: Clock[F]) extends HealthService[F] {
  override def serviceInformation(): F[ServiceInformation] =
    clock.realTime
      .flatMap(duration => ServiceInformation.create[F](new DateTime(duration.toMillis), buildInformation))
}
