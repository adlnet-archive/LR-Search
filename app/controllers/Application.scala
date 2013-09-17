package controllers
import play.api.Play.current
import org.elasticsearch.search.SearchHit
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import scala.collection.JavaConversions._
import play.api.cache.Cached
object Application extends Controller {

  def index = Cached("homePage") {
    Action { 
      Ok(views.html.index())
    }
  }
}
