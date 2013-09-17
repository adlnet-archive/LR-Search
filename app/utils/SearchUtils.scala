package utils
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.search.sort.SortOrder
import com.sksamuel.elastic4s.SuggestMode.{ Missing, Popular }
import com.sksamuel.elastic4s.Analyzer._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.get.GetResponse
import dispatch.{ url, Http, as }
import scala.util.parsing.json._
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.TermFilterBuilder
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor
object SearchUtils {
  val client = ElasticClient.remote("localhost", 9300)
  val pageSize = 25
  def searchLR(query: String, page: Int): Future[SearchResponse] = {
    client.search(search in "lr" start (page * pageSize) limit pageSize query {
      bool {
        should(
          matchPhrase("title", query) boost 10 slop 3 cutoffFrequency 3.4 setLenient true,
          matchPhrase("description", query) boost 5 slop 3 cutoffFrequency 3.4 setLenient true,
          term("standards", query),
          term("keys", query))
      }
    })
  }
  def similiar(docId: String): Future[SearchResponse] = {
    client.execute {
      morelike id docId in "lr/lr_doc" minTermFreq 1 percentTermsToMatch 0.2 minDocFreq 1
    }
  }
  def standard(standard: String, page: Int): Future[SearchResponse] = {
    val svc = url("http://localhost:5984/standards/_design/standards/_list/just-keys/children") <<? Map("key" -> ("\"" + standard + "\""))
    val resp = Http(svc OK as.String)
    resp.flatMap { result =>      
      val rawStandards = JSON.parseRaw(result)
      val standards = rawStandards.get.asInstanceOf[JSONArray]
      var parsedStandards = standards.list.map(s => term("standards", s.toString))     
      client.search(search in "lr" start (page * pageSize) limit pageSize query {
        bool {
          should(parsedStandards:_*)
        }
      })
    }
  }
  def getDoc(docId: String): Future[GetResponse] = {
    client.get(get id docId from "lr/lr_doc")
  }
}
