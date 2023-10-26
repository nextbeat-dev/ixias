/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import sbt._

object Dependencies {

  val play = "org.playframework" %% "play" % "3.0.0"
  val playJson = "org.playframework" %% "play-json" % "3.0.0"

  val connectorJava = "mysql" % "mysql-connector-java" % "5.1.39"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

  val slick = "com.typesafe.slick" %% "slick" % "3.3.2"

  val hikariCP = "com.zaxxer" % "HikariCP" % "2.5.0"

  val keyczar = "org.keyczar" % "keyczar" % "0.71h"

  val uapScala = "org.uaparser" %% "uap-scala" % "0.8.0"

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
  ).map("org.specs2" %% _ % "4.5.1" % Test)

  val awsSdkVersion = "1.12.129"
  object aws {
    val s3 = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
    val sns = "com.amazonaws" % "aws-java-sdk-sns" % awsSdkVersion
    val cloudfront = "com.amazonaws" % "aws-java-sdk-cloudfront" % awsSdkVersion
  }

  val qldb = "software.amazon.qldb" % "amazon-qldb-driver-java" % "1.0.1"

  val jacksonDataformat = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-ion"  % "2.10.0"
  val jacksonModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.0"
}
