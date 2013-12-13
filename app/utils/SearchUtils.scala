package utils

import scala.concurrent.Future
import scala.util.parsing.json._
import org.elasticsearch.action.search.SearchResponse
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import dispatch._
import com.ning.http.client.Response
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import traits.ResultToJson
import play.api.Play
import traits._
import com.fasterxml.jackson.annotation.JsonValue
case class SearchBoosts(val titlePhraseBoost: Int, val titleBoost: Int, val descriptionPhraseBoost: Int, val descriptionBoost: Int)
class SearchUtils {
  this: SearchClientContainer with ResultFormatter[JsValue] with UrlContainer with BoostContainer =>
  val pageSize = 25
  import play.api.Play.current
  def createQuery(termQuery: Seq[String], filters: Option[Seq[String]]): QueryDefinition = {
    def processedFilters(filters: Seq[String]) = {
      val filterQueries = filters.map(f => matches("accessMode", f)).toList 
      val accessFilters = queryFilter(must(filterQueries: _*))
      val generalFilters = filters.flatMap(f => List(
        queryFilter(matches("keys", f)),
        queryFilter(matchPhrase("publisher", f)))).toList
      accessFilters :: generalFilters
    }
    def baseQuery(termQuerys: Seq[String]) = bool {
      val queries = termQuerys.flatMap { t =>
        List(
          matchPhrase("title", t) boost boost.titlePhraseBoost setLenient true,
          matches("title", t) boost boost.titleBoost,
          matchPhrase("description", t) boost boost.descriptionPhraseBoost setLenient true,
          matches("description", t) boost boost.descriptionBoost,
          term("standards", t),
          matches("keys", t))
      }
      should(queries: _*)
    }
    filters match {
      case Some(filters) =>
        filteredQuery query {
          baseQuery(termQuery)
        } filter {
          should(processedFilters(filters): _*)
        }
      case None => baseQuery(termQuery)
    }
  }
  def similiar(docId: String): Future[Option[JsValue]] = {
    client.execute {
      morelike id docId in "lr/lr_doc" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }.map(format)
  }
  def searchLR(standard: String, page: Int, filter: Option[Seq[String]]): Future[Option[JsValue]] = {
    val svc = url(dbUrl) / "_design" / "standards" / "_list" / "just-values" / "children" <<? Map("key" -> ("\"" + standard + "\""), "stale" -> "update_after")
    val resp: Future[Either[Throwable, Response]] = Http(svc).either
    resp.flatMap { result =>
      result match {
        case Left(t) => Future(None)
        case Right(result) =>
          val rawBody = result.getResponseBody()          
          val rawStandards = JSON.parseRaw(rawBody)          
          val parsedStandards = rawStandards.map { x =>
            x match {
              case js: JSONObject => List(standard)
              case js: JSONArray => js.asInstanceOf[JSONArray].list.map(_.toString)            
            }            
          }
          parsedStandards match {
            case Some(Nil) => client.search(search in "lr" start (page * pageSize) limit pageSize query {
              createQuery(List(standard), filter)
            }).map(format)
            case Some(s) => client.search(search in "lr" start (page * pageSize) limit pageSize query {
              createQuery(s, filter)
            }).map(format)
            case None => Future(None)
          }
      }
    }

  }
  def searchByPublisher(publisher: String, page: Int): Future[Option[JsValue]] = {
    client.search(search in "lr" start (page * pageSize) limit pageSize query {
      matchPhrase("publisher", publisher)
    }).map(format)
  }
}
