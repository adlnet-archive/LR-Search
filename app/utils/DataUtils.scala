package utils

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.Future
import org.elasticsearch.action.count.CountResponse
import org.elasticsearch.action.get.GetResponse
import play.api.libs.json._
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits._
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.SearchHit

object DataUtils extends ResultToJson { 
  def data(client: ElasticClient)(): Future[CountResponse] = {
    client.execute(
      count from "lr")
  }
  def doc(client: ElasticClient)(docId: String): Future[Option[JsValue]] = {
    client.get(get id docId from "lr/lr_doc").map(x => format(docId)(Right(x)))
  }
}