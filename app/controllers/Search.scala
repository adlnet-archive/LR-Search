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
object Search extends Controller with ESClient {
  import play.api.Play.current
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")

  def search(terms: String, page: Option[Int], filter: Option[String]) = Action.async { request =>
    val parsedFilters = filter.map { str =>
      str.split(";").toSeq
    }
    if (terms != null) {
      val result = SearchUtils.searchLR(client)(terms, page.getOrElse(0), parsedFilters)
      result.map(r => {
        r match {
          case Some(js) => Ok(js)
          case None => NotFound
        }
      })
    } else {
      Future(NotFound)
    }
  }

  def similiar(id: String) = Action.async { request =>
    val result = SearchUtils.similiar(client)(id)
    result.map { r =>
      r match {
        case Some(js) => Ok(js)
        case None => NotFound
      }
    }
  }
  def standards(standard: String, page: Option[Int] = None) = Action.async { request =>
    val result = SearchUtils.standard(client, url)(standard, page.getOrElse(0))
    result.map { r =>
      r match {
        case Some(js) => Ok(js)
        case None => NotFound
      }
    }
  }
}