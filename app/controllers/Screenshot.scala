package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator
import traits.ESClient

object Screenshot extends Controller with ESClient {
  import play.api.Play.current
  val url = Play.application.configuration.getString("couchdb.db.metadata.url").getOrElse("http://localhost:5984/lr-data")
  def getScreenshot(docId: String) = Action.async { request =>
    ScreenshotUtils.getScreenshot(url, client)(docId)
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