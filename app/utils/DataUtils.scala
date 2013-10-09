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
import scala.concurrent.{Await}
import scala.concurrent.duration._
import java.io.InputStream

object DataUtils extends ResultToJson { 
  def data(client: ElasticClient)(): Future[CountResponse] = {
    client.execute(
      count from "lr")
  }
  def doc(client: ElasticClient)(docId: String): Future[Option[JsValue]] = {
    client.get(get id docId from "lr/lr_doc").map(x => format(docId)(Right(x)))
  }
  def docFromCouchdb(dbUrl: String)(docId:String): Future[InputStream] = {
    val std = url(dbUrl) / docId
    val resp = Http(std)
    resp.map(d => d.getResponseBodyAsStream())	 
  }
  def docs(client:ElasticClient)(docIds: Seq[String]): Future[Option[JsValue]] = {
    client.search(search in "lr" filter {
      idsFilter(docIds:_*)
    }).map(format)
  }
}