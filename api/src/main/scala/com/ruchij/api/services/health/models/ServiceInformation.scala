package com.ruchij.api.services.health.models

import cats.effect.kernel.Sync
import cats.implicits.toFunctorOps
import com.eed3si9n.ruchij.api.BuildInfo
import com.ruchij.core.config.BuildInformation
import org.joda.time.DateTime

import scala.util.Properties

case class ServiceInformation(
  serviceName: String,
  serviceVersion: String,
  organization: String,
  scalaVersion: String,
  sbtVersion: String,
  javaVersion: String,
  gitBranch: Option[String],
  gitCommit: Option[String],
  buildTimestamp: Option[DateTime],
  timestamp: DateTime
)

object ServiceInformation {
  def create[F[_]: Sync](timestamp: DateTime, buildInformation: BuildInformation): F[ServiceInformation] =
    Sync[F].delay(Properties.javaVersion)
      .map { javaVersion =>
        ServiceInformation(
          BuildInfo.name,
          BuildInfo.version,
          BuildInfo.organization,
          BuildInfo.scalaVersion,
          BuildInfo.sbtVersion,
          javaVersion,
          buildInformation.gitBranch,
          buildInformation.gitCommit,
          buildInformation.buildTimestamp,
          timestamp
        )
      }
}
