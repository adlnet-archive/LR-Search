package utils
import java.io.InputStream
import dispatch._
import Defaults._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.Logger
import scala.sys.process._
import scala.concurrent.Await
import scala.concurrent.duration._
import traits._
import scala.async.Async.{ async, await }
import scalax.io._
import scala.io._
import scala.io.Source._
import java.io.BufferedInputStream
import java.io.File
class ScreenshotUtils {
  this: SearchClientContainer with UrlContainer =>
  val currentLocation = System.getProperty("user.dir")
  private def serveFile(docId: String): Option[File] = {
    val destination = s"$currentLocation/screenshots/$docId"
    try {
      val f = new java.io.File(destination)
      if (f.exists()) Some(f) else None
    } catch {
      case t: Throwable => None
    }
  }
  private def takeScreenShot(docId: String, siteUrl: String): Option[File] = {
    val destination = s"$currentLocation/screenshots/$docId"
    val exec = s"xvfb-run --auto-servernum --server-num=1 python $currentLocation/screenshots.py $siteUrl $destination"
    val result = exec.!!
    serveFile(docId)
  }
  private def getScreenShot(docId: String): Future[Option[File]] = {
    async {
      //try to serve the file from disk
      serveFile(docId) match {
        case Some(data) => Some(data)
        //if not found take pic and save to disk
        case None =>
          val doc = await { client.execute(get id docId from "lr/lr_doc") }
          val siteUrl = doc.getSource().get("url").asInstanceOf[String]
          takeScreenShot(docId, siteUrl)
      }
    }
  }
  def getScreenshot(docId: String): Future[Option[File]] = {
    if (docId == "{{result._id}}") Future(None)
    else getScreenShot(docId)
  }
}		