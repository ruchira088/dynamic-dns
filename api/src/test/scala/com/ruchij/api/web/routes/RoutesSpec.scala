package com.ruchij.api.web.routes

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class RoutesSpec extends AnyFlatSpec with Matchers {
//  "GET /service" should "return a successful response containing service information" in {
//    val dateTime = DateTime.now()
//    implicit val clock: Clock[IO] = stubClock[IO](dateTime)
//
//    val application = HttpTestApp[IO]()
//
//    val request = Request[IO](uri = Uri(path = "/service"))
//
//    val response = application.run(request).unsafeRunSync()
//
//    val expectedJsonResponse =
//      json"""{
//        "serviceName": "dynamic-dns",
//        "serviceVersion": "0.0.1",
//        "organization": "com.ruchij",
//        "scalaVersion": "2.13.3",
//        "sbtVersion": "1.3.13",
//        "javaVersion": ${Properties.javaVersion},
//        "timestamp": $dateTime
//      }"""
//
//    response must beJsonContentType
//    response must haveJson(expectedJsonResponse)
//    response must haveStatus(Status.Ok)
//  }
}
