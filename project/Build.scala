import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "lrsearch"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    cache,
    "org.scala-lang.modules" %% "scala-async" % "0.9.0-M2",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "com.sksamuel.elastic4s" % "elastic4s_2.10" % "0.90.5.2")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    scalaVersion := "2.10.2")
}
