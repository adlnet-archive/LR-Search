package controllers
import play.api.Play.current
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
object Data extends Controller with ESClient {
  val url = Play.application.configuration.getString("couchdb.db.metadata.url").getOrElse("http://localhost:5984/lr-data")
  def data(keys: Option[String]) = 
    Action.async { request =>
      keys match {
        case Some(docIds) => {
          val firstParse = java.net.URLDecoder.decode(docIds, "utf-8").replace("\\\"", "\"")
          val docs = Json.parse(firstParse).as[Seq[String]]
          DataUtils.docs(client)(docs).map(result =>
            result match {
              case Some(js) => Ok(js)
              case None => Ok(Json.toJson(Map(
                  "count" -> Json.toJson(0),
                  "data" -> Json.toJson(Seq[String]())
                  )))
            })
        }
        case None => {
          val result = DataUtils.data(client)
          result.map { r =>
            Ok(Json.toJson(Map(
              "doc_count" -> Json.toJson(r.getCount()))))
          }
        }
      }
    }
  
  def doc(docId: String) = 
    Action.async { request =>
      docId.toLowerCase() match {
        case "sitemap" => {
          val raw = DataUtils.docFromCouchdb(url)(docId)
          raw.map { r =>
            SimpleResult(
              header = ResponseHeader(200, Map("Content-Type" -> "application/json")),
              body = Enumerator.fromStream(r, 256))
          }
        }
        case _ => {
          val result = DataUtils.doc(client)(docId)
          result.map { r =>
            r match {
              case Some(js) => Ok(js)
              case None => NotFound
            }
          }
        }
      }
    }
  }
