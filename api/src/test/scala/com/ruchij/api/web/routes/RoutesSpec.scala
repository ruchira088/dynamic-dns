package com.ruchij.api.web.routes

import cats.effect.{Clock, IO}
import cats.effect.unsafe.implicits.global
import com.eed3si9n.ruchij.api.BuildInfo
import com.ruchij.api.test.HttpTestApp
import com.ruchij.api.test.matchers.{beJsonContentType, haveJson, haveStatus}
import com.ruchij.api.test.utils.Providers.stubClock
import com.ruchij.core.circe.Encoders.dateTimeEncoder
import io.circe.literal.JsonStringContext
import org.http4s.{Request, Status}
import org.http4s.implicits.http4sLiteralsSyntax
import org.joda.time.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.util.Properties

class RoutesSpec extends AnyFlatSpec with Matchers {
  "GET /service" should "return a successful response containing service information" in {
    val dateTime = DateTime.now()
    implicit val clock: Clock[IO] = stubClock[IO](dateTime)

    val application = HttpTestApp[IO]

    val request = Request[IO](uri = uri"/health")

    val response = application.run(request).unsafeRunSync()

    val expectedJsonResponse =
      json"""{
        "serviceName": "dynamic-dns-api",
        "serviceVersion": ${BuildInfo.version},
        "organization": "com.ruchij",
        "scalaVersion": ${BuildInfo.scalaVersion},
        "sbtVersion": ${BuildInfo.sbtVersion},
        "javaVersion": ${Properties.javaVersion},
        "gitBranch": ${BuildInfo.gitBranch},
        "gitCommit": ${BuildInfo.gitCommit},
        "buildTimestamp": ${new DateTime(BuildInfo.buildTimestamp.toEpochMilli)},
        "timestamp": $dateTime
      }"""

    response must beJsonContentType
    response must haveJson(expectedJsonResponse)
    response must haveStatus(Status.Ok)
  }
}
