package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator
import play.api.cache.Cached

object Standards extends Controller {
  import play.api.Play.current
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")
  def standards() =
    Action.async { request =>
      val std = StandardsUtil.standards(url)()
      std.map { data =>
        SimpleResult(
          header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
          body = Enumerator.fromStream(data, 256))
      }
    }

  def standard(standardId: String) =
    Action.async { request =>
      val std = StandardsUtil.getStandard(url)(standardId)
      std.map { data =>
        SimpleResult(
          header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
          body = Enumerator.fromStream(data, 256))
      }
    }
}