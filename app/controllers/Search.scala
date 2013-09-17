package controllers
import org.elasticsearch.search.SearchHit
import scala.concurrent._
import org.elasticsearch.action.search.SearchResponse
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import scala.collection.JavaConversions._
object Search extends Controller {
  def formatSearchResult(hit: SearchHit): JsValue = {
    val tmp = hit.sourceAsMap()
    Json.toJson(Map(
      "_id" -> Json.toJson(hit.id()),
      "title" -> Json.toJson(tmp.getOrElse("title", "").toString),
      "publisher" -> Json.toJson(tmp.getOrElse("publisher", "").toString),
      "description" -> Json.toJson(tmp.getOrElse("description", "").toString),
      "hasScreenshot" -> Json.toJson(tmp.getOrElse("hasScreenshot", false).asInstanceOf[Boolean]),
      "url" -> Json.toJson(tmp.getOrElse("url", "").toString)))
  }
  def search(terms: String, page: Option[Int]) = Action { request =>
    if (terms != null) {
      val result = SearchUtils.searchLR(terms, page.getOrElse(0))
      Async {
        result.map(r => {
          val data = r.getHits().hits().map(formatSearchResult)
          Ok(Json.toJson(Map(
            "count" -> Json.toJson(r.getHits().totalHits()),
            "data" -> Json.toJson(data))))
        })
      }
    } else {
      Ok(Json.toJson(
        Map(
          "count" -> Json.toJson(0),
          "data" -> Json.toJson(Seq[JsValue]()))))
    }
  }
  def similiar(id: String) = Action { request =>
    val result = SearchUtils.similiar(id)
    Async {
      result.map(r => {
        val data = r.getHits().hits().map(formatSearchResult)
        Ok(Json.toJson(Map(
          "count" -> Json.toJson(r.getHits().totalHits()),
          "data" -> Json.toJson(data))))
      })
    }
  }
  def standards(standard: String, page: Option[Int] = None) = Action { request =>
    val result = SearchUtils.standard(standard, page.getOrElse(0))
    Async {
      result.map(r => {
        val data = r.getHits().hits().map(formatSearchResult)
        Ok(Json.toJson(Map(
          "count" -> Json.toJson(r.getHits().totalHits()),
          "data" -> Json.toJson(data))))
      })
    }
  }
}