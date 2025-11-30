import Dependencies.*
import sbtrelease.ReleaseStateTransformations.*

import java.time.Instant
import scala.language.postfixOps
import scala.sys.process.*
import scala.util.Try

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := Dependencies.ScalaVersion,
    maintainer := "me@ruchij.com",
    scalacOptions ++= Seq("-Xlint", "-feature", "-Wconf:cat=lint-byname-implicit:s"),
    addCompilerPlugin(kindProjector),
    addCompilerPlugin(betterMonadicFor),
  )
}

lazy val core = (project in file("./core")).settings(libraryDependencies ++= Seq(http4sCirce, jodaTime, pureconfig))

lazy val api =
  (project in file("./api"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "dynamic-dns-api",
      buildInfoPackage := "com.eed3si9n.ruchij.api",
      buildInfoKeys := Seq(name, organization, version, scalaVersion, sbtVersion, buildTimestamp, gitBranch, gitCommit),
      topLevelDirectory := None,
      libraryDependencies ++= Seq(
        http4sDsl,
        http4sEmberServer,
        http4sCirce,
        circeGeneric,
        circeParser,
        circeLiteral,
        jodaTime,
        pureconfig,
        logbackClassic
      ) ++ Seq(scalaTest, pegdown).map(_ % Test)
    )
    .dependsOn(core)

lazy val serverless =
  (project in file("./serverless"))
    .settings(
      name := "dynamic-dns-serverless-api",
      assembly / assemblyJarName := s"${name.value}.jar",
      assembly / assemblyMergeStrategy := {
        case PathList("META-INF", _*) => MergeStrategy.discard
        case _ => MergeStrategy.first
      },
      libraryDependencies ++= Seq(awsLambdaJavaCore, awsLambdaJavaEvents)
    )
    .dependsOn(api)

lazy val syncJob =
  (project in file("./sync-job"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "dynamic-dns-sync-job",
      buildInfoPackage := "com.eed3si9n.ruchij.job",
      buildInfoKeys := Seq(name, organization, version, scalaVersion, sbtVersion, buildTimestamp, gitBranch, gitCommit),
      Universal / javaOptions ++= Seq("-Dlogback.configurationFile=/opt/data/logback.xml"),
      topLevelDirectory := None,
      libraryDependencies ++= Seq(
        http4sEmberClient,
        http4sCirce,
        circeGeneric,
        pureconfig,
        route53,
        awsSns,
        phoneNumber,
        logbackClassic,
        logstashLogbackEncoder
      )
    )
    .dependsOn(core)

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  pushChanges,
  setNextVersion,
  commitNextVersion
)

lazy val buildTimestamp = BuildInfoKey.action("buildTimestamp") { Instant.now() }
lazy val gitBranch = BuildInfoKey.action("gitBranch") { runGitCommand("git rev-parse --abbrev-ref HEAD") }
lazy val gitCommit = BuildInfoKey.action("gitCommit") { runGitCommand("git rev-parse --short HEAD") }

def runGitCommand(command: String): Option[String] = {
  val gitFolder = new File(".git")

  if (gitFolder.exists()) Try(command !!).toOption.map(_.trim).filter(_.nonEmpty) else None
}