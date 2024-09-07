import Dependencies._
import sbtrelease.ReleaseStateTransformations._

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
      buildInfoKeys := Seq(name, organization, version, scalaVersion, sbtVersion),
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
      assembly / assemblyJarName := s"${name.value}-${version.value}.jar",
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
      buildInfoKeys := Seq(name, organization, version, scalaVersion, sbtVersion),
      topLevelDirectory := None,
      libraryDependencies ++= Seq(
        http4sEmberClient,
        http4sCirce,
        circeGeneric,
        pureconfig,
        route53,
        awsSns,
        phoneNumber,
        logbackClassic
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
  setNextVersion,
  commitNextVersion,
  pushChanges
)