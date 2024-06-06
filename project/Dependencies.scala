import sbt._

object Dependencies
{
  val ScalaVersion = "2.13.14"
  val Http4sVersion = "0.23.27"
  val CirceVersion = "0.14.7"
  val AwsSdkVersion = "2.25.67"

  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion

  lazy val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Http4sVersion

  lazy val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Http4sVersion

  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion

  lazy val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion

  lazy val circeParser = "io.circe" %% "circe-parser" % CirceVersion

  lazy val circeLiteral = "io.circe" %% "circe-literal" % CirceVersion

  lazy val jodaTime = "joda-time" % "joda-time" % "2.12.7"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.17.6"

  lazy val route53 = "software.amazon.awssdk" % "route53" % AwsSdkVersion

  lazy val awsLambdaJavaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.3"

  lazy val awsLambdaJavaEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.11.5"

  lazy val awsSns = "software.amazon.awssdk" % "sns" % AwsSdkVersion

  lazy val phoneNumber = "com.googlecode.libphonenumber" % "libphonenumber" % "8.13.38"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.5.6"

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full

  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % "0.3.1"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}
