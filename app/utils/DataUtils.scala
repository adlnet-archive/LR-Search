package utils

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.Future
import org.elasticsearch.action.count.CountResponse
import org.elasticsearch.action.get.GetResponse
import play.api.libs.json._
import dispatch._
import Defaults._
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit
import scala.concurrent.{ Await }
import scala.concurrent.duration._
import java.io.InputStream
import traits.ResultToJson
import traits._
import scala.async.Async.{ async, await }
class DataUtils {
  this: SearchClientContainer with ResultFormatter[JsValue] with UrlContainer =>
  def data(): Future[CountResponse] = { client.execute(count from indexName) }
  def doc(docId: String): Future[Option[JsValue]] = {
    async { format(docId, await { client.execute(get id docId from s"$indexName/$documentType") }) }
  }
  def docFromCouchdb(docId: String): Future[InputStream] = {
    async { (await { Http(url(dbUrl) / docId) }).getResponseBodyAsStream() }
  }
  def docs(docIds: Seq[String]): Future[Option[JsValue]] = {
    async { format(await { client.execute(search in indexName filter { idsFilter(docIds: _*) }) }) }
  }
}