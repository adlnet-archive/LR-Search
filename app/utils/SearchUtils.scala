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
object SearchUtils extends ResultToJson {
  val pageSize = 25
  def createQuery(termQuery: String, filters: Option[Seq[String]]): QueryDefinition = {
    def processedFilters(filters: Seq[String]) = {
      filters.flatMap(f => List(termFilter("accessMode", f), termFilter("mediaFeatures", f)))
    }    
    val baseQuery = bool {
      should(
        matchPhrase("title", termQuery) boost 10 slop 3 cutoffFrequency 3.4 setLenient true,
        matchPhrase("description", termQuery) boost 5 slop 3 cutoffFrequency 3.4 setLenient true,
        term("standards", termQuery),
        term("keys", termQuery))
    }
    filters match {
      case Some(filters) =>
        filteredQuery query {
          baseQuery
        } filter {
          should(processedFilters(filters): _*)
        }
      case None => baseQuery
    }
  }
  def searchLR(client: ElasticClient)(query: String, page: Int, filter: Option[Seq[String]]): Future[Option[JsValue]] = {
    client.search(search in "lr" start (page * pageSize) limit pageSize query {
      createQuery(query, filter)
    }).map(format)
  }
  def similiar(client: ElasticClient)(docId: String): Future[Option[JsValue]] = {
    client.execute {
      morelike id docId in "lr/lr_doc" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }.map(format)
  }
  def standard(client: ElasticClient, dbUrl: String)(standard: String, page: Int): Future[Option[JsValue]] = {
    val svc = url(dbUrl) / "_design" / "standards" / "_list" / "just-keys" / "children" <<? Map("key" -> ("\"" + standard + "\""))
    val resp = Http(svc OK as.String)
    resp.flatMap { result =>
      val rawStandards = JSON.parseRaw(result)
      val parsedStandards = rawStandards.map { x =>
        x.asInstanceOf[JSONArray].list.map(s => term("standards", s.toString))
      }
      parsedStandards match {
        case Some(s) => client.search(search in "lr" start (page * pageSize) limit pageSize query {
          bool {
            should(s: _*)
          }
        }).map(format)
        case None => Future(None)
      }
    }
  }
}
