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
class ScreenshotUtils {
  this: SearchClientContainer with UrlContainer =>
  private def serveFile(docId: String) = {
    val currentLocation = System.getProperty("user.dir")
    val destination = s"$currentLocation/screenshots/$docId.jpg"
    try {
      Some(new java.io.FileInputStream(destination))
    } catch {
      case t: Throwable => None
    }
  }
  private def takeScreenShot(docId: String): Future[Option[InputStream]] = {
    async {
      val doc = await { client.execute(get id docId from "lr/lr_doc") }
      val siteUrl = doc.getSource().get("url").asInstanceOf[String]
      val currentLocation = System.getProperty("user.dir")
      val destination = s"$currentLocation/screenshots/$docId.jpg"
      val exec = s"xvfb-run --auto-servernum --server-num=1 python $currentLocation/screenshots.py $siteUrl $destination"
      val result = exec.!!
      serveFile(docId)
    }
  }
  private def getFromCouchdb(docId: String) = {
    async {
      val std = url(dbUrl) / docId / "screenshot.jpeg"
      val resp = await { Http(std) }
      resp.getStatusCode() match {
        case 200 =>
          val currentLocation = System.getProperty("user.dir")
          val destination = s"$currentLocation/screenshots/$docId.jpg"
          val outFile = new java.io.FileOutputStream(destination)
          val remoteFile = resp.getResponseBodyAsStream()
          try {
            var buffer = new Array[Byte](256)
            while(remoteFile.read(buffer) > 0){
              outFile.write(buffer)
            }
          } finally {
            outFile.close()
          }
          remoteFile.reset()
          Some(remoteFile)
        case _ => await { takeScreenShot(docId) }
      }
    }
  }
  private def getScreenShot(docId: String): Future[Option[InputStream]] = {
    async {
      serveFile(docId) match {
        case Some(data) => Some(data)
        case None       => await { getFromCouchdb(docId) }
      }
    }
  }
  def getScreenshot(docId: String): Future[Option[InputStream]] = {
    if (docId == "{{result._id}}") Future(None)
    else getScreenShot(docId)
  }
}	