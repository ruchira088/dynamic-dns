import sbt._

object Dependencies
{
  val ScalaVersion = "2.13.10"
  val Http4sVersion = "0.23.18"
  val CirceVersion = "0.14.5"
  val AwsSdkVersion = "2.20.51"

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  lazy val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Http4sVersion

  lazy val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Http4sVersion

  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion

  lazy val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion

  lazy val circeParser = "io.circe" %% "circe-parser" % CirceVersion

  lazy val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion

  lazy val jodaTime = "joda-time" % "joda-time" % "2.12.5"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.17.3"

  lazy val route53 = "software.amazon.awssdk" % "route53" % AwsSdkVersion

  lazy val awsLambdaJavaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.2"

  lazy val awsLambdaJavaEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.11.1"

  lazy val awsSns = "software.amazon.awssdk" % "sns" % AwsSdkVersion

  lazy val phoneNumber = "com.googlecode.libphonenumber" % "libphonenumber" % "8.13.10"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.4.7"

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full

  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % "0.3.1"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}
