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
object Search extends Controller {
  import play.api.Play.current
  val searchUtil = new SearchUtils with RemoteClientFromConfig with ResultToJson with UrlFromConfig with BoosFromConfigFile
  val emptyResult = Ok(Json.toJson(Map(
    "count" -> Json.toJson(0),
    "data" -> Json.toJson(List[String]()))))
  def search(terms: String, page: Option[Int], filter: Option[String]) = Cached(terms + page.getOrElse(0) + filter.getOrElse("")) {
    Action.async { request =>
      val parsedFilters: Option[Seq[String]] = filter.map(_.split(";"))
      val result = searchUtil.searchLR(terms, page.getOrElse(0), parsedFilters)
      result.map {
        case Some(js) => Ok(js)
        case None => emptyResult
      }
    }
  }
  def similiar(id: String) = Cached(id) {
    Action.async { request =>
      val result = searchUtil.similiar(id)
      result.map {
        case Some(js) => Ok(js)
        case None => emptyResult
      }
    }
  }
}