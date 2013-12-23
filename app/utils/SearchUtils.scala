package utils
import scala.async.Async._
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
      filters.flatMap(f => List(
        queryFilter(matches("accessMode", f)),
        queryFilter(matches("keys", f)),
        queryFilter(matchPhrase("publisher", f)))).toSeq
    }
    def baseQuery(termQueries: Seq[String]) = bool {
      val queries = termQueries.flatMap { t =>
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
      morelike id docId in s"$indexName/$documentType" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }.map(format)
  }
  def searchLR(standard: String, page: Int, filter: Option[Seq[String]]): Future[Option[JsValue]] = {
    def runQuery(s: List[String]): Future[Option[JsValue]] = {
      client.search(search in indexName start (page * pageSize) limit pageSize query {
        customScore script "_score + (doc.containsKey('paraScore') ? doc['paraScore'] : 0)" lang "mvel" query createQuery(s, filter) boost 1
      }).map(format)
    }
    async {
      val svc = url(dbUrl) / "_design" / "standards" / "_list" / "just-values" / "children" <<? Map("key" -> ("\"" + standard + "\""), "stale" -> "update_after")
      val result: Either[Throwable, Response] = await { Http(svc).either }
      result match {
        case Left(t) => None
        case Right(result) =>
          val rawBody = result.getResponseBody()
          val rawStandards = JSON.parseRaw(rawBody)
          rawStandards match {
            case Some(js: JSONObject) => await { runQuery(List(standard)) }
            case Some(js: JSONArray) => await { runQuery(standard :: js.asInstanceOf[JSONArray].list.map(_.toString)) }
            case None => None
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
