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
import Workflows._

ThisBuild / crossScalaVersions         := Seq(scala213)
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin(java11), JavaSpec.temurin(java17))
ThisBuild / githubWorkflowBuildPreamble ++= List(
  dockerRun,
  waitForLocalStackContainerStart,
  waitForMySQLContainerStart,
  settingsSns,
  settingsS3,
  settingsSES
)
ThisBuild / githubWorkflowBuildPostamble += dockerStop

ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    "scalafmt",
    "Scalafmt",
    githubWorkflowJobSetup.value.toList ::: List(
      WorkflowStep.Run(
        List("sbt scalafmtCheckAll 'project /' scalafmtSbtCheck"),
        name = Some("Scalafmt check")
      )
    ),
    scalas = List(scala213),
    javas  = List(JavaSpec.temurin(java11))
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
  .settings(
    libraryDependencies ++= Seq(
      shapeless,
      typesafeConfig,
      playJson,
      keyczar,
      uapScala,
      commonsCodec,
      slf4jApi,
      munit
    ) ++ cats
  )

lazy val ixiasSlick = IxiaSProject("ixias-slick", "framework/ixias-slick")
  .settings(
    libraryDependencies ++= Seq(
      slick,
      hikariCP,
      connectorJava % Test
    )
  )
  .dependsOn(ixiasCore)

lazy val ixiasMail = IxiaSProject("ixias-mail", "framework/ixias-mail")
  .settings(
    libraryDependencies ++= Seq(
      guice,
      twilio,
      commonsEmail
    )
  )
  .dependsOn(ixiasCore)

lazy val ixiasAws = IxiaSProject("ixias-aws-core", "framework/ixias-aws/core")
  .settings(libraryDependencies += aws.core)
  .dependsOn(ixiasCore)

lazy val ixiasAwsSns = IxiaSProject("ixias-aws-sns", "framework/ixias-aws/sns")
  .settings(libraryDependencies ++= Seq(aws.sns, munit))
  .dependsOn(ixiasAws)

lazy val ixiasAwsS3 = IxiaSProject("ixias-aws-s3", "framework/ixias-aws/s3")
  .settings(libraryDependencies ++= Seq(aws.s3, munit))
  .dependsOn(ixiasAws)

lazy val ixiasAwsSes = IxiaSProject("ixias-aws-ses", "framework/ixias-aws/ses")
  .settings(libraryDependencies ++= Seq(aws.ses, munit))
  .dependsOn(ixiasAws)

lazy val ixiasAwsS3Lib = IxiaSProject("ixias-aws-s3-lib", "framework/ixias-aws-s3-lib")
  .settings(libraryDependencies ++= Seq(aws.cloudfront, munit, connectorJava % Test))
  .dependsOn(ixiasSlick, ixiasAwsS3)

// IxiaS Play Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasPlayCore = IxiaSProject("ixias-play-core", "framework/ixias-play-core")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore)

lazy val ixiasPlayAuth = IxiaSProject("ixias-play-auth", "framework/ixias-play-auth")
  .settings(libraryDependencies ++= Seq(play, munit))
  .dependsOn(ixiasCore, ixiasPlayCore)

lazy val docs = (project in file("docs"))
  .settings(
    description    := "Documentation for IxiaS",
    scalacOptions  := Nil,
    publish / skip := true,
    mdocIn         := baseDirectory.value / "src" / "main" / "mdoc",
    paradoxTheme   := Some(builtinParadoxTheme("generic")),
    paradoxProperties ++= Map(
      "org"           -> organization.value,
      "scalaVersion"  -> scalaVersion.value,
      "awsSDKVersion" -> awsSdkVersion,
      "version"       -> version.value.takeWhile(_ != '+')
    ),
    Compile / paradox / sourceDirectory := mdocOut.value,
    Compile / paradoxRoots              := List("index.html"),
    makeSite                            := makeSite.dependsOn(mdoc.toTask("")).value,
    git.remoteRepo                      := "git@github.com:nextbeat-dev/ixias.git",
    ghpagesNoJekyll                     := true
  )
  .settings(commonSettings)
  .dependsOn(ixiasCore, ixiasSlick, ixiasMail, ixiasAwsSns, ixiasPlayCore, ixiasPlayAuth)
  .enablePlugins(MdocPlugin, SitePreviewPlugin, ParadoxSitePlugin, GhpagesPlugin)

// IxiaS Meta Packages
//~~~~~~~~~~~~~~~~~~~~~
lazy val ixias = IxiaSProject("ixias", ".")
  .aggregate(
    ixiasCore,
    ixiasSlick,
    ixiasMail,
    ixiasAws,
    ixiasAwsSns,
    ixiasAwsS3,
    ixiasAwsSes,
    ixiasAwsS3Lib,
    ixiasPlayCore,
    ixiasPlayAuth,
    docs
  )
  .dependsOn(ixiasCore)
