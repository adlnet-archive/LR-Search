package controllers
import play.api.Play.current
import scala.async.Async.{ async, await }
import scala.concurrent._
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
import play.api.libs.iteratee.Enumerator
import play.api.cache.Cached
object Data extends Controller {
  val dataUtil = new DataUtils with RemoteClientFromConfig with ResultToJson with UrlFromConfig
  val emptyResult = Ok(Json.toJson(Map(
    "count" -> Json.toJson(0),
    "data" -> Json.toJson(Seq[String]()))))
  def data(keys: Option[String]) =
    Action.async { request =>
      keys match {
        case Some(docIds) =>
          val firstParse = java.net.URLDecoder.decode(docIds, "utf-8").replace("\\\"", "\"")
          val docs = Json.parse(firstParse).as[Seq[String]]
          async {
            val result = await { dataUtil.docs(docs) }
            result match {
              case Some(js) => Ok(js)
              case None => emptyResult
            }
          }
        case None =>
          async {
            val result = await { dataUtil.data }
            Ok(Json.toJson(Map("doc_count" -> Json.toJson(result.getCount()))))
          }
      }
    }

  def doc(docId: String) =
    Action.async { request =>
      docId.toLowerCase() match {
        case "sitemap" => {
          async {
            val r = await { dataUtil.docFromCouchdb(docId) }
            SimpleResult(header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
              body = Enumerator.fromStream(r, 256))

          }
        }
        case _ => {
          async {
            await { dataUtil.doc(docId) } match {
              case Some(js) => Ok(js)
              case None => NotFound
            }
          }
        }
      }
    }
}
