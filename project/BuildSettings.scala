/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

import scala.sys.process._

import ScalaVersions._

object BuildSettings {

  private val branch  = "git branch".lineStream_!.find(_.head == '*').map(_.drop(2)).getOrElse("")
  private val release = branch == "master" || branch.startsWith("release")

  private val baseScalaSettings: Seq[String] = Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-encoding",
    "utf8",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions"
  )

  /** These settings are used by all projects. */
  val commonSettings = Seq(
    organization       := "net.ixias",
    homepage           := Some(url("https://nextbeat-dev.github.io/ixias/")),
    scalaVersion       := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    resolvers ++= Seq(
      "Nextbeat Releases" at "https://s3-ap-northeast-1.amazonaws.com/maven.ixias.net/releases"
    ),
    Test / fork := true
  )

  private val publisherSettings = Seq(
    publishTo := {
      val path = if (release) "releases" else "snapshots"
      Some("Nextbeat snapshots" at "s3://maven.nextbeat.net.s3-ap-northeast-1.amazonaws.com/" + path)
    },
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  object IxiaSProject {
    def apply(name: String, dir: String): Project =
      Project(name, file(dir))
        .settings(commonSettings: _*)
        .settings(publisherSettings: _*)
        .settings(scalacOptions ++= baseScalaSettings)
  }
}
