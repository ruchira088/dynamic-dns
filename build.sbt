import Dependencies._

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := SCALA_VERSION,
    maintainer := "me@ruchij.com",
    scalacOptions ++= Seq("-Xlint", "-feature", "-Wconf:cat=lint-byname-implicit:s"),
    addCompilerPlugin(kindProjector),
    addCompilerPlugin(betterMonadicFor),
    addCompilerPlugin(scalaTypedHoles)
  )
}

lazy val core = (project in file("./core")).settings(libraryDependencies ++= Seq(http4sCirce))

lazy val web =
  (project in file("./web"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "dynamic-dns-web",
      version := "0.0.1",
      buildInfoPackage := "com.eed3si9n.ruchij.web",
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

lazy val dnsSyncJob =
  (project in file("./dnsSyncJob"))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "dynamic-dns-sync-job",
      version := "0.0.1",
      buildInfoPackage := "com.eed3si9n.ruchij.job",
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      topLevelDirectory := None
    )

addCommandAlias("testWithCoverage", "; coverage; test; coverageReport")
