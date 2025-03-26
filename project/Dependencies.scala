/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import sbt._

object Dependencies {

  val play     = "org.playframework" %% "play"      % "3.0.6"
  val playJson = "org.playframework" %% "play-json" % "3.0.4"

  val connectorJava = "com.mysql" % "mysql-connector-j" % "8.3.0"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.12"

  val typesafeConfig = "com.typesafe" % "config" % "1.4.3"

  val slick = "com.typesafe.slick" %% "slick" % "3.5.2"

  val hikariCP = "com.zaxxer" % "HikariCP" % "5.1.0"

  val keyczar = "org.keyczar" % "keyczar" % "0.71h"

  val uapScala = "org.uaparser" %% "uap-scala" % "0.18.0"

  val commonsCodec = "commons-codec" % "commons-codec" % "1.18.0"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "2.0.17"

  val guice = "com.google.inject" % "guice" % "6.0.0"

  val twilio = "com.twilio.sdk" % "twilio-java-sdk" % "6.3.0"

  val commonsEmail = "org.apache.commons" % "commons-email" % "1.6.0"

  val cats: Seq[ModuleID] = Seq(
    "cats-kernel",
    "cats-core"
  ).map("org.typelevel" %% _ % "2.10.0")

  val munit = "org.scalameta" %% "munit" % "1.1.0" % Test

  val awsSdkVersion = "2.31.9"
  object aws {
    val core       = "software.amazon.awssdk" % "aws-core"   % awsSdkVersion
    val s3         = "software.amazon.awssdk" % "s3"         % awsSdkVersion
    val sns        = "software.amazon.awssdk" % "sns"        % awsSdkVersion
    val ses        = "software.amazon.awssdk" % "ses"        % awsSdkVersion
    val cloudfront = "software.amazon.awssdk" % "cloudfront" % awsSdkVersion
  }
}
