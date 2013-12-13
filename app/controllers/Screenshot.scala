package controllers

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
    screenShotUtil.getScreenshot(docId)
      .map { data =>
        data match {
          case Some(d) => SimpleResult(
            header = ResponseHeader(200, Map("Content-Type" -> "image/jpeg")),
            body = Enumerator.fromStream(d, 256))
          case None => NotFound
        }
      }
  }
}