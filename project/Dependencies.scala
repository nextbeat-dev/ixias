/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import sbt._

object Dependencies {

  val play     = "org.playframework" %% "play"      % "3.0.2"
  val playJson = "org.playframework" %% "play-json" % "3.0.2"

  val connectorJava = "com.mysql" % "mysql-connector-j" % "8.3.0"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.10"

  val typesafeConfig = "com.typesafe" % "config" % "1.4.3"

  val slick = "com.typesafe.slick" %% "slick" % "3.5.0"

  val hikariCP = "com.zaxxer" % "HikariCP" % "5.1.0"

  val keyczar = "org.keyczar" % "keyczar" % "0.71h"

  val uapScala = "org.uaparser" %% "uap-scala" % "0.16.0"

  val commonsCodec = "commons-codec" % "commons-codec" % "1.16.0"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "2.0.9"

  val guice = "com.google.inject" % "guice" % "6.0.0"

  val twilio = "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0"

  val commonsEmail = "org.apache.commons" % "commons-email" % "1.5"

  val cats: Seq[ModuleID] = Seq(
    "cats-kernel",
    "cats-core"
  ).map("org.typelevel" %% _ % "2.10.0")

  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.5.2"

  val specs2: Seq[ModuleID] = Seq(
    "specs2-core",
    "specs2-matcher-extra"
  ).map("org.specs2" %% _ % "4.5.1" % Test)

  val munit = "org.scalameta" %% "munit" % "0.7.29" % Test

  val awsSdkVersion = "1.12.671"
  object aws {
    val core       = "com.amazonaws" % "aws-java-sdk-core"       % awsSdkVersion
    val s3         = "com.amazonaws" % "aws-java-sdk-s3"         % awsSdkVersion
    val sns        = "com.amazonaws" % "aws-java-sdk-sns"        % awsSdkVersion
    val ses        = "com.amazonaws" % "aws-java-sdk-ses"        % awsSdkVersion
    val cloudfront = "com.amazonaws" % "aws-java-sdk-cloudfront" % awsSdkVersion
  }
}
