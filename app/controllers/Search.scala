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
  val titlePhraseBoost = Play.application.configuration.getInt("search.title.phrase.boost").getOrElse(5)
  val descriptionPhraseBoot = Play.application.configuration.getInt("search.desc.phrase.boost").getOrElse(4)
  val titleBoost = Play.application.configuration.getInt("search.title.boost").getOrElse(3)
  val descriptionBoost = Play.application.configuration.getInt("search.desc.boost").getOrElse(2)  
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")
  val boost: SearchBoosts = SearchBoosts(titlePhraseBoost, titleBoost, descriptionPhraseBoot, descriptionPhraseBoot)
  
  def search(terms: String, page: Option[Int], filter: Option[String]) = Cached(terms + page.getOrElse(0) + filter.getOrElse("")) {
    Action.async { request =>
      val parsedFilters: Option[Seq[String]] = filter.map(_.split(";"))
      val result = SearchUtils.searchLR(client, url, boost)(terms, page.getOrElse(0), parsedFilters)
      result.map {
        case Some(js) => Ok(js)
        case None => Ok(Json.toJson(Map(
          "count" -> Json.toJson(0),
          "data" -> Json.toJson(List[String]()))))
      }
    }
  }
  def similiar(id: String) = Cached(id) {
    Action.async { request =>
      val result = SearchUtils.similiar(client)(id)
      result.map {
        case Some(js) => Ok(js)
        case None => Ok(Json.toJson(Map(
          "count" -> Json.toJson(0),
          "data" -> Json.toJson(List[String]()))))
      }
    }
  }
}