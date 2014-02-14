package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator
import play.api.cache.Cached
import play.api.Play.current
import traits.UrlFromConfig
import scala.async.Async.{ async, await }
object Standards extends Controller {
  val standardsUtil = new StandardsUtil with UrlFromConfig
  def standards() =
    Action.async { request =>
      async {
        val data = await { standardsUtil.standards() }
        SimpleResult(
          header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
          body = Enumerator.fromStream(data, 256))
      }
    }

  def standard(standardId: String) =
    Action.async { request =>
      async {
        val data = await { standardsUtil.getStandard(standardId) }
        SimpleResult(
          header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
          body = Enumerator.fromStream(data, 256))
      }
    }

}