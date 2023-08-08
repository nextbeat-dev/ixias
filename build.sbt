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

ThisBuild / crossScalaVersions         := Seq(scala212)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin(java8))

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Run(
    List("sbt '++ ${{ matrix.scala }}' ixias-cache/compile"),
    name = Some("Deprecated project compile")
  ),
  WorkflowStep.Run(
    List("docker-compose -f framework/ixias-core/src/test/docker/docker-compose.yml up -d"),
    name = Some("Set up Docker")
  )
)

ThisBuild / githubWorkflowBuild ++= Seq(
  WorkflowStep.Run(
    List("docker-compose -f framework/ixias-core/src/test/docker/docker-compose.yml down"),
    name = Some("Close Docker")
  )
)

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
    slick,
    playJson,
    hikariCP,
    keyczar,
    uapScala,
    commonsCodec,
    slf4jApi,
    connectorJava % Test,
    logbackClassic % Test
  ) ++ cats ++ specs2)

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

lazy val ixiasAwsS3 = IxiaSProject("ixias-aws-s3", "framework/ixias-aws-s3")
  .settings(libraryDependencies ++= Seq(
    aws.s3,
    aws.cloudfront
  ))
  .dependsOn(ixiasCore)

lazy val ixiasAwsQLDB = IxiaSProject("ixias-aws-qldb", "framework/ixias-aws-qldb")
  .settings(libraryDependencies ++= Seq(
    qldb,
    jacksonDataformat,
    jacksonModule
  ))
  .dependsOn(ixiasCore)

// IxiaS Play Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasPlayCore = IxiaSProject("ixias-play-core", "framework/ixias-play-core")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore)

lazy val ixiasPlayScalate = IxiaSProject("ixias-play-scalate", "framework/ixias-play-scalate")
  .settings(libraryDependencies ++= Seq(
    play,
    scalateCore
  ))
  .dependsOn(ixiasCore)

lazy val ixiasPlayAuth = IxiaSProject("ixias-play-auth", "framework/ixias-play-auth")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore, ixiasPlayCore)

// IxiaS Deprecated Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasCache = IxiaSProject("ixias-cache", "framework/ixias-cache")
  .settings(libraryDependencies += shade)
  .dependsOn(ixiasCore)

// IxiaS Meta Packages
//~~~~~~~~~~~~~~~~~~~~~
lazy val ixias = IxiaSProject("ixias", ".")
  .aggregate(ixiasCore, ixiasMail, ixiasAws, ixiasPlay)
  .dependsOn(ixiasCore, ixiasMail)

lazy val ixiasAws = IxiaSProject("ixias-aws", "target/ixias-aws")
  .aggregate(ixiasCore, ixiasAwsSns, ixiasAwsS3, ixiasAwsQLDB)
  .dependsOn(ixiasCore, ixiasAwsSns, ixiasAwsS3, ixiasAwsQLDB)

lazy val ixiasPlay = IxiaSProject("ixias-play", "target/ixias-play")
  .aggregate(ixiasPlayCore, ixiasPlayScalate, ixiasPlayAuth)
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
