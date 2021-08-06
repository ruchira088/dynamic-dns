package com.ruchij.api.test

import cats.effect.{Clock, Sync}
import com.ruchij.api.services.health.HealthServiceImpl
import com.ruchij.api.web.Routes
import com.ruchij.core.config.BuildInformation
import org.http4s.HttpApp

object HttpTestApp {
  def apply[F[_]](implicit sync: Sync[F], clock: Clock[F]): HttpApp[F] =
    Routes(new HealthServiceImpl[F](BuildInformation(Some("my-branch"), Some("my-commit"), None))(sync, clock, sync))
}
