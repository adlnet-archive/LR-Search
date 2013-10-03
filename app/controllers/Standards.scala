package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator

object Standards extends Controller {
  import play.api.Play.current
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")
  def standards() = Action { request =>

    val std = StandardsUtil.standards(url)()
    Async {
      std.map { data =>
        val resp = Enumerator.fromStream(data)
        Ok.stream(resp).withHeaders("Content-Type" -> "application/json")
      }
    }
  }
  def standard(standardId: String) = Action { request =>
    val std = StandardsUtil.getStandard(url)(standardId)
    Async {
      std.map { data =>
        val resp = Enumerator.fromStream(data)
        Ok.stream(resp).withHeaders("Content-Type" -> "application/json")
      }
    }
  }
}