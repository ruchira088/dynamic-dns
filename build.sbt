import Dependencies._

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := Dependencies.ScalaVersion,
    maintainer := "me@ruchij.com",
    scalacOptions ++= Seq("-Xlint", "-feature", "-Wconf:cat=lint-byname-implicit:s"),
    addCompilerPlugin(kindProjector),
    addCompilerPlugin(betterMonadicFor),
    addCompilerPlugin(scalaTypedHoles)
  )
}

lazy val core = (project in file("./core")).settings(libraryDependencies ++= Seq(http4sCirce, jodaTime, pureconfig))

lazy val api =
  (project in file("./api"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "dynamic-dns-api",
      version := "0.0.1",
      buildInfoPackage := "com.eed3si9n.ruchij.api",
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      topLevelDirectory := None,
      libraryDependencies ++= Seq(
        http4sDsl,
        http4sBlazeServer,
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
      version := "0.0.1",
      assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
      assemblyMergeStrategy in assembly := {
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
      version := "0.0.1",
      buildInfoPackage := "com.eed3si9n.ruchij.job",
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      topLevelDirectory := None,
      libraryDependencies ++= Seq(http4sBlazeClient, http4sCirce, circeGeneric, pureconfig, route53, logbackClassic)
    )
    .dependsOn(core)

addCommandAlias("cleanAll", "; serverless/clean; api/clean; syncJob/clean; core/clean")
addCommandAlias("compileAll", "; core/compile; syncJob/compile; api/compile; serverless/compile")
addCommandAlias("refreshAll", "; cleanAll; compileAll")

