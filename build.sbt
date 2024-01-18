/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import ScalaVersions._
import JavaVersions._
import Dependencies._
import BuildSettings._

ThisBuild / crossScalaVersions         := Seq(scala213)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin(java11))

// IxiaS Core Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasCore = IxiaSProject("ixias-core", "framework/ixias-core")
  .settings(
    javaOptions ++= Seq(
      "-Dlogback.configurationFile=logback.xml"
    )
  )
  .settings(libraryDependencies ++= Seq(
    shapeless,
    typesafeConfig,
    playJson,
    keyczar,
    uapScala,
    commonsCodec,
    slf4jApi,
    connectorJava % Test,
    logbackClassic % Test
  ) ++ cats ++ specs2)

lazy val ixiasSlick = IxiaSProject("ixias-slick", "framework/ixias-slick")
  .settings(libraryDependencies ++= Seq(
    slick,
    hikariCP,
    connectorJava % Test,
  ))
  .dependsOn(ixiasCore)

lazy val ixiasMail = IxiaSProject("ixias-mail", "framework/ixias-mail")
  .settings(libraryDependencies ++= Seq(
    guice,
    twilio,
    commonsEmail
  ))
  .dependsOn(ixiasCore)

lazy val ixiasAwsSns = IxiaSProject("ixias-aws-sns", "framework/ixias-aws-sns")
  .settings(libraryDependencies += aws.sns)
  .dependsOn(ixiasCore)

// IxiaS Play Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasPlayCore = IxiaSProject("ixias-play-core", "framework/ixias-play-core")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore)

lazy val ixiasPlayAuth = IxiaSProject("ixias-play-auth", "framework/ixias-play-auth")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore, ixiasPlayCore)

// IxiaS Meta Packages
//~~~~~~~~~~~~~~~~~~~~~
lazy val ixias = IxiaSProject("ixias", ".")
  .aggregate(ixiasCore, ixiasMail, ixiasSlick, ixiasAws, ixiasPlay)
  .dependsOn(ixiasCore, ixiasMail)

lazy val ixiasAws = IxiaSProject("ixias-aws", "target/ixias-aws")
  .aggregate(ixiasCore, ixiasAwsSns)
  .dependsOn(ixiasCore, ixiasAwsSns)

lazy val ixiasPlay = IxiaSProject("ixias-play", "target/ixias-play")
  .aggregate(ixiasPlayCore, ixiasPlayAuth)
  .dependsOn(ixiasPlayCore, ixiasPlayAuth)

// Setting for prompt
import com.scalapenos.sbt.prompt._
val defaultTheme = PromptTheme(List(
  text("[SBT] ", fg(green)),
  text(state => { Project.extract(state).get(organization) + "@" }, fg(magenta)),
  text(state => { Project.extract(state).get(name) },               fg(magenta)),
  text(":", NoStyle),
  gitBranch(clean = fg(green), dirty = fg(yellow)).padLeft("[").padRight("]"),
  text(" > ", NoStyle)
))
promptTheme := defaultTheme
shellPrompt := (implicit state => promptTheme.value.render(state))
