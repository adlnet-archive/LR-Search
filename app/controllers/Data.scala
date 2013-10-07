package controllers
import play.api.Play.current
import org.elasticsearch.search.SearchHit
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import traits._
import scala.collection.JavaConversions._
import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.action.get.GetResponse
import views.html.defaultpages.notFound
object Data extends Controller with ESClient {

  def data() = Action.async { request =>
    val result = DataUtils.data(client)
    result.map { r =>
      Ok(Json.toJson(Map(
        "doc_count" -> Json.toJson(r.getCount()))))
    }
  }
  def doc(docId: String) = Action.async { request =>
    val result = DataUtils.doc(client)(docId)
    result.map { r =>
      r match {
        case Some(js) => Ok(js)
        case None => NotFound
      }
    }
  }
}