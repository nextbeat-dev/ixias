/*
 * This file is part of the IxiaS service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

import scala.sys.process._

import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._

object BuildSettings {

  private val branch = "git branch".lineStream_!.find(_.head == '*').map(_.drop(2)).getOrElse("")
  private val release = branch == "master" || branch.startsWith("release")

  private val baseScalaSettings: Seq[String] = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:-unused,_", // Enable recommended additional warnings.
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ypartial-unification" // Add support for partial unification of type constructors
  )

  /** These settings are used by all projects. */
  private val commonSettings = Seq(
    organization := "net.ixias",
    scalaVersion := "2.12.11",
    resolvers ++= Seq(
      "Nextbeat Releases" at "https://s3-ap-northeast-1.amazonaws.com/maven.ixias.net/releases"
    ),
    fork in Test := true,
  )

  private val publisherSettings = Seq(
    publishTo := {
      val path = if (release) "releases" else "snapshots"
      Some("Nextbeat snapshots" at "s3://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/" + path)
    },
    publishArtifact in(Compile, packageDoc) := !release, // disable publishing the Doc jar for production
    publishArtifact in(Compile, packageSrc) := !release, // disable publishing the sources jar for production
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
  }
}
