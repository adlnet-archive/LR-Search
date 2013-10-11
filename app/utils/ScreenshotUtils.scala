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
    resp.flatMap { d =>
      d.getStatusCode() match {
        case 200 => Future(Some(d.getResponseBodyAsStream()))
        case 404 =>
          val doc = client.get(get id docId from "lr/lr_doc")
          doc.flatMap { d =>
            val siteUrl = d.getSource().get("url").asInstanceOf[String]
            try {
              val currentLocation =System.getProperty("user.dir") 
              Logger.debug(currentLocation)
              val exec = s"xvfb-run --auto-servernum --server-num=1 python $currentLocation/screenshots.py $siteUrl $docId $dbUrl"                
              val result = exec.!!
              val std = url(dbUrl) / docId / "screenshot.jpeg"
              val resp = Http(std)
              resp.map(resp => Some(resp.getResponseBodyAsStream()))
            }catch {
              case e:java.lang.RuntimeException =>
                Logger.error("Error taking screenshot", e)
                Future(None)
            }
          }
        case _ => Future(None)
      }
    }
  }
}