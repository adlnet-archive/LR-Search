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

object ScreenshotUtils {
  def getScreenshot(dbUrl: String, client: ElasticClient)(docId: String): Future[Option[InputStream]] = {
    val std = url(dbUrl) / docId / "screenshot.jpeg"
    val resp = Http(std)
    Logger.debug(System.getProperty("user.dir"))
    resp.map { d =>
      d.getStatusCode() match {
        case 200 => Some(d.getResponseBodyAsStream())
        case 404 =>
          val doc = client.get(get id docId from "lr/lr_doc")
          val siteUrl = doc().getSource().get("url")
          val exec = Seq("xvfb-run", "--auto-servernum", "--server-num=1", "python", "screenshots.py", siteUrl, docId, dbUrl).mkString(" ").mkString(" ").!!
          val std = url(dbUrl) / docId / "screenshot.jpeg"
          val resp = Await.result(Http(std), Duration(5, SECONDS))
          Some(resp.getResponseBodyAsStream())
        case _ => None
      }
    }
  }
}