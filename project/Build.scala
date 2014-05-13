import sbt._
import Keys._
import play.Project._
import com.typesafe.sbt.SbtAtmosPlay.atmosPlaySettings
object ApplicationBuild extends Build {

  val appName = "lrsearch"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    cache,
    "com.typesafe.akka" %% "akka-actor" % "2.2.0",
    "org.scala-lang.modules" %% "scala-async" % "0.9.0-M2",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "com.sksamuel.elastic4s" % "elastic4s_2.10" % "1.1.1.1")

  val main = play.Project(appName, appVersion, appDependencies).settings(atmosPlaySettings: _*).settings(
    resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )
}
