package controllers
import scala.async.Async.{ async, await }
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator
import traits._

object Screenshot extends Controller {
  val screenShotUtil: ScreenshotUtils = new ScreenshotUtils with RemoteClientFromConfig with UrlFromConfig
  def getScreenshot(docId: String) = Action.async { request =>
    async {
      val result = await { screenShotUtil.getScreenshot(docId) }
      result match {
        case Some(d) => SimpleResult(
          header = ResponseHeader(200, Map("Content-Type" -> "image/jpeg")),
          body = Enumerator.fromFile(d, 256))
        case None => NotFound
      }
    }
  }
}