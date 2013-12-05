package controllers
import play.Logger
import play.api._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.async.Async.{ async, await }
import play.api.libs.ws.WS.WSRequestHolder
object FlagController extends Controller {
  val url = Play.application.configuration.getString("couchdb.db.flagged.url").getOrElse("http://localhost:5984/flagged")
  def acceptFlag = Action.async { request =>
    request.body.asJson match {
      case Some(data) => async {
        val resp = await { WS.url(url).post(data) }
        Ok(Json.toJson(Map("result" -> true)))
      }
      case None => async { BadRequest(Json.toJson(Map("result" -> false))) }
    }
  }
  def flaggedItems = Action.async {
    async {
      val resp = await {

        WS.url(url + "/_design/flagged/_view/all").withQueryString("include_docs" -> "true").get
      }
      val docs = Json.parse(resp.body) \\ "doc"
      Ok(Json.toJson(Map("flaggedItems" -> docs)))
    }
  }
  def flagsForDoc(docId: String) = Action.async {
    async {
      val resp = await {
        WS.url(url + "/_design/flagged/_view/all").withQueryString("include_docs" -> "true", "key" -> ("\"" + docId + "\"")).get
      }
      val docs = Json.parse(resp.body) \\ "doc"
      Ok(Json.toJson(Map("flaggedItems" -> docs)))
    }
  }
  def flagsReason(reason: String) = Action.async {
    async {
      val resp = await {
        WS.url(url + "/_design/flagged/_view/reason").withQueryString("include_docs" -> "true", "key" -> ("\"" + reason + "\"")).get
      }
      val docs = Json.parse(resp.body) \\ "doc"
      Ok(Json.toJson(Map("flaggedItems" -> docs)))
    }
  }
}