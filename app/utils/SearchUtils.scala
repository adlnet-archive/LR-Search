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
  import play.api.Play.current
  val pageSize = 25
  def processAccessibilityMetadata(accessibiltiyOptions: Seq[String]) = {
    must(accessibiltiyOptions.map(f => queryFilter(matches("accessMode", f))): _*)
  }
  def processedFilters(filters: Seq[String]) = {
    filters.flatMap(f => List(
      queryFilter(matches("keys", f)),
      queryFilter(matchPhrase("publisher", f)))).toList
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
  def createFilteredQuery(termQuery: Seq[String], filters: FilterDefinition) = {
    filteredQuery query {
      baseQuery(termQuery)
    } filter {
      filters
    }
  }
  def createQuery(termQuery: Seq[String], filters: Option[Seq[String]], accessibilityOptions: Option[Seq[String]]): QueryDefinition = {
    val filterCombinations = (filters, accessibilityOptions)
    filterCombinations match {
      case (Some(filters), Some(accessibilityOptions)) =>
        val totalFilters = processAccessibilityMetadata(accessibilityOptions) :: processedFilters(filters)
        createFilteredQuery(termQuery, should(totalFilters: _*))
      case (Some(filters), None) => createFilteredQuery(termQuery, should(processedFilters(filters): _*))
      case (None, Some(accessibilityOptions)) => createFilteredQuery(termQuery, processAccessibilityMetadata(accessibilityOptions))
      case (None, None) => baseQuery(termQuery)
    }
  }
  def similiar(docId: String): Future[Option[JsValue]] = {
    client.execute {
      morelike id docId in s"$indexName/$documentType" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }.map(format)
  }
  def searchLR(standard: String, page: Int, filter: Option[Seq[String]], contentType: Option[String], accessibilityOptions: Option[Seq[String]]): Future[Option[JsValue]] = {
    def runQuery(s: List[String]): Future[Option[JsValue]] = {
      client.search(search in indexName start (page * pageSize) limit pageSize query {
        customScore script "_score + (doc.containsKey('paraScore') ? doc['paraScore'].value : 0)" lang "mvel" query createQuery(s, filter, accessibilityOptions) boost 1
      }).map(format)
    }
    def processExpandedQuery(result: Response) = {
      async {
        val rawBody = result.getResponseBody()
        val rawStandards = JSON.parseRaw(rawBody)
        rawStandards match {
          case Some(js: JSONObject) => await { runQuery(List(standard)) }
          case Some(js: JSONArray) => await { runQuery(standard :: js.asInstanceOf[JSONArray].list.map(_.toString)) }
          case None => None
        }
      }
    }
    async {
      val svc = url(dbUrl) / "_design" / "standards" / "_list" / "just-values" / "children" <<? Map("key" -> ("\"" + standard + "\""), "stale" -> "update_after")
      val result: Either[Throwable, Response] = await { Http(svc).either }
      result match {
        case Left(t) =>
          Logger.error("Error pulling standards from couchdb", t)
          None
        case Right(result) => await { processExpandedQuery(result) }
      }
    }
  }
  def searchByPublisher(publisher: String, page: Int): Future[Option[JsValue]] = {
    client.search(search in indexName start (page * pageSize) limit pageSize query {
      matchPhrase("publisher", publisher)
    }).map(format)
  }
}
