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
class ScreenshotUtils {
  this: SearchClientContainer with UrlContainer =>
  private def takeScreenShot(docId: String) = {
    async {
      val doc = await { client.get(get id docId from "lr/lr_doc") }
      val siteUrl = doc.getSource().get("url").asInstanceOf[String]
      val currentLocation = System.getProperty("user.dir")
      val exec = s"xvfb-run --auto-servernum --server-num=1 python $currentLocation/screenshots.py $siteUrl $docId $dbUrl"
      val result = exec.!!
    }
  }
  
  private def getScreenShotWithFailover(docId: String, retries: Int = 0): Future[Option[InputStream]] = {
    async {
      if (retries >= 3) None
      else {
        val std = url(dbUrl) / docId / "screenshot.jpeg"
        val resp = await { Http(std) }
        resp.getStatusCode() match {
          case 200 => Some(resp.getResponseBodyAsStream())
          case 404 =>
            await { takeScreenShot(docId) }
            await { getScreenShotWithFailover(docId, retries+1) }
          case _ => None
        }
      }
    }
  }
  def getScreenshot(docId: String): Future[Option[InputStream]] = {
    if (docId == "{{result._id}}") Future(None)
    else getScreenShotWithFailover(docId)
  }
}	