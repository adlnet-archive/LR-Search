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
import traits._
import scala.collection.JavaConversions._
import com.sksamuel.elastic4s.ElasticClient
import play.api.cache.Cached
object Search extends Controller with ESClient {
  import play.api.Play.current
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")

  def search(terms: String, page: Option[Int], filter: Option[String]) = Cached(terms + page.getOrElse(0) + filter.getOrElse("")) {
    Action.async { request =>
      val parsedFilters: Option[Seq[String]] = filter.map(_.split(":"))
      val result = SearchUtils.searchLR(client, url)(terms, page.getOrElse(0), parsedFilters)
      result.map(r => {
        r match {
          case Some(js) => Ok(js)
          case None => Ok(Json.toJson(Map(
            "count" -> Json.toJson(0),
            "data" -> Json.toJson(List[String]()))))
        }
      })
    }
  }
  def similiar(id: String) = Cached(id) {
    Action.async { request =>
      val result = SearchUtils.similiar(client)(id)
      result.map { r =>
        r match {
          case Some(js) => Ok(js)
          case None => Ok(Json.toJson(Map(
            "count" -> Json.toJson(0),
            "data" -> Json.toJson(List[String]()))))
        }
      }
    }
  }
}