import sbt._

object Dependencies {

  val play = "com.typesafe.play" %% "play" % "2.7.5"
  val playJson = "com.typesafe.play" %% "play-json" % "2.7.4"

  val connectorJava = "mysql" % "mysql-connector-java" % "5.1.39"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  val config = "com.typesafe" % "config" % "1.3.0"

  val slick = "com.typesafe.slick" %% "slick" % "3.2.1"

  val shade = "io.monix" %% "shade" % "1.9.5"

  val hikariCP = "com.zaxxer" % "HikariCP" % "2.5.0"

  val keyczar = "org.keyczar" % "keyczar" % "0.71h"

  val uapScala = "org.uaparser" %% "uap-scala" % "0.1.0"

  val jodaTime = "joda-time" % "joda-time" % "2.9.4"

  val commonsCodec = "commons-codec" % "commons-codec" % "1.10"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.21"

  val guice = "com.google.inject" % "guice" % "4.1.0"

  val twilio = "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0"

  val commonsEmail = "org.apache.commons" % "commons-email" % "1.4"

  val cats: Seq[ModuleID] = Seq(
    "cats-kernel",
    "cats-core"
  ).map("org.typelevel" %% _ % "2.1.1")

  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"

  val specs2: Seq[ModuleID] = Seq(
    "specs2-core",
    "specs2-matcher-extra"
  ).map("org.specs2" %% _ % "3.9.1" % Test)

  val awsSdkVersion = "1.12.129"
  object aws {
    val s3 = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
    val sns = "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion
    val cloudfront = "com.amazonaws" % "aws-java-sdk-cloudfront" % awsSdkVersion
  }

  val qldb = "software.amazon.qldb" % "amazon-qldb-driver-java" % "1.0.1"

  val jacksonDataformat = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-ion"  % "2.10.0"
  val jacksonDatatype = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"  % "2.10.0"
  val jacksonModule = "com.fasterxml.jackson.module" % "jackson-module-scala"  % "2.10.0"

  def scalaCompiler(version: String): ModuleID = "org.scala-lang" % "scala-compiler" % version
  val scalateCore = "org.scalatra.scalate" %% "scalate-core" % "1.8.0"
}
