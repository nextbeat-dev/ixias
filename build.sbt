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
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin(java8), JavaSpec.temurin(java11))

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
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
    javas  = List(JavaSpec.temurin(java8))
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
      slick,
      playJson,
      hikariCP,
      keyczar,
      uapScala,
      commonsCodec,
      slf4jApi,
      connectorJava  % Test,
      logbackClassic % Test
    ) ++ cats ++ specs2
  )

lazy val ixiasMail = IxiaSProject("ixias-mail", "framework/ixias-mail")
  .settings(
    libraryDependencies ++= Seq(
      guice,
      twilio,
      commonsEmail
    )
  )
  .dependsOn(ixiasCore)

lazy val ixiasAwsSns = IxiaSProject("ixias-aws-sns", "framework/ixias-aws-sns")
  .settings(libraryDependencies += aws.sns)
  .dependsOn(ixiasCore)

lazy val ixiasAwsS3 = IxiaSProject("ixias-aws-s3", "framework/ixias-aws-s3")
  .settings(
    libraryDependencies ++= Seq(
      aws.s3,
      aws.cloudfront
    )
  )
  .dependsOn(ixiasCore)

lazy val ixiasAwsQLDB = IxiaSProject("ixias-aws-qldb", "framework/ixias-aws-qldb")
  .settings(
    libraryDependencies ++= Seq(
      qldb,
      jacksonDataformat,
      jacksonModule
    )
  )
  .dependsOn(ixiasCore)

// IxiaS Play Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasPlayCore = IxiaSProject("ixias-play-core", "framework/ixias-play-core")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore)

lazy val ixiasPlayAuth = IxiaSProject("ixias-play-auth", "framework/ixias-play-auth")
  .settings(libraryDependencies += play)
  .dependsOn(ixiasCore, ixiasPlayCore)

lazy val docs = (project in file("docs"))
  .settings(
    description    := "Documentation for IxiaS",
    scalacOptions  := Nil,
    publish / skip := true,
    mdocIn         := baseDirectory.value / "src" / "main" / "mdoc",
    paradoxTheme   := Some(builtinParadoxTheme("generic")),
    paradoxProperties ++= Map(
      "org"          -> organization.value,
      "scalaVersion" -> scalaVersion.value,
      "version"      -> version.value.takeWhile(_ != '+')
    ),
    Compile / paradox / sourceDirectory := mdocOut.value,
    Compile / paradoxRoots              := List("index.html"),
    makeSite                            := makeSite.dependsOn(mdoc.toTask("")).value,
    git.remoteRepo                      := "git@github.com:nextbeat-dev/ixias.git",
    ghpagesNoJekyll                     := true
  )
  .settings(commonSettings)
  .dependsOn(ixiasCore, ixiasMail, ixiasAwsSns, ixiasAwsS3, ixiasPlayCore, ixiasPlayAuth)
  .enablePlugins(MdocPlugin, SitePreviewPlugin, ParadoxSitePlugin, GhpagesPlugin)

// IxiaS Meta Packages
//~~~~~~~~~~~~~~~~~~~~~
lazy val ixias = IxiaSProject("ixias", ".")
  .aggregate(ixiasCore, ixiasMail, ixiasAws, ixiasPlay, docs)
  .dependsOn(ixiasCore, ixiasMail)

lazy val ixiasAws = IxiaSProject("ixias-aws", "target/ixias-aws")
  .aggregate(ixiasCore, ixiasAwsSns, ixiasAwsS3, ixiasAwsQLDB)
  .dependsOn(ixiasCore, ixiasAwsSns, ixiasAwsS3, ixiasAwsQLDB)

lazy val ixiasPlay = IxiaSProject("ixias-play", "target/ixias-play")
  .aggregate(ixiasPlayCore, ixiasPlayAuth)
  .dependsOn(ixiasPlayCore, ixiasPlayAuth)
