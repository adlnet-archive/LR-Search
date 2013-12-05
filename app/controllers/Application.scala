package controllers
import play.api.Play.current
import play.api.cache._
import org.elasticsearch.search.SearchHit
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import scala.collection.JavaConversions._
object Application extends Controller {

  def index = Cached("index") {
    Action {
      Ok(views.html.index())
    }
  }
  def review = Cached("review") {
    Action {
      Ok(views.html.review())
    }
  }
  def reviewDocument(docId: String) = Cached("reviewDoc") {
    Action {
      Ok(views.html.view(docId))
    }
  }
  def cors(junk: String) = Action {
    Ok("")
  }
}
