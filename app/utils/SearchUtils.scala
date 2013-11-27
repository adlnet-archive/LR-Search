package utils

import scala.concurrent.Future
import scala.util.parsing.json._
import org.elasticsearch.action.search.SearchResponse
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import dispatch._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.Logger
import traits.ResultToJson
import play.api.Play
case class SearchBoosts(val titlePhraseBoost: Int, val titleBoost: Int, val descriptionPhraseBoost: Int, val descriptionBoost: Int)
object SearchUtils extends ResultToJson {
  val pageSize = 25
  import play.api.Play.current
  def createQuery(termQuery: Seq[String], filters: Option[Seq[String]], boost: SearchBoosts): QueryDefinition = {
    def processedFilters(filters: Seq[String]) = {
      filters.flatMap(f => List(
        queryFilter(matches("accessMode", f)),
        queryFilter(matches("mediaFeatures", f)),
        queryFilter(matches("keys", f)),
        queryFilter(matchPhrase("publisher", f))))
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
  def similiar(client: ElasticClient)(docId: String): Future[Option[JsValue]] = {
    client.execute {
      morelike id docId in "lr/lr_doc" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }.map(format)
  }
  def searchLR(client: ElasticClient, dbUrl: String, boost: SearchBoosts)(standard: String, page: Int, filter: Option[Seq[String]]): Future[Option[JsValue]] = {
    Logger.debug(boost.titlePhraseBoost.toString)
    val svc = url(dbUrl) / "_design" / "standards" / "_list" / "just-values" / "children" <<? Map("key" -> ("\"" + standard + "\""), "stale" -> "update_after")
    val resp = Http(svc OK as.String)
    resp.flatMap { result =>
      val rawStandards = JSON.parseRaw(result)
      val parsedStandards = rawStandards.map { x =>
        x.asInstanceOf[JSONArray].list.map(_.toString)
      }
      parsedStandards match {
        case Some(Nil) => client.search(search in "lr" start (page * pageSize) limit pageSize query {
          createQuery(List(standard), filter, boost)
        }).map(format)
        case Some(s) => client.search(search in "lr" start (page * pageSize) limit pageSize query {
          createQuery(s, filter, boost)
        }).map(format)
        case None => Future(None)
      }
    }
  }
  def searchByPublisher(client: ElasticClient)(publisher: String, page: Int): Future[Option[JsValue]] = {
    client.search(search in "lr" start (page * pageSize) limit pageSize query {
      matchPhrase("publisher", publisher)
    }).map(format)
  }
}
