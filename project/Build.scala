import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "lrsearch"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "com.newrelic.agent.java" % "newrelic-api" % "2.x.x",
    "com.sksamuel.elastic4s" % "elastic4s_2.10" % "0.90.4.0-SNAPSHOT")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += (
      "Local Ivy2 Repository" at "file://" + Path.userHome.absolutePath + "/.ivy2/local"),
    resolvers += (
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"))
}
