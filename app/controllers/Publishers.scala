package controllers
import scala.async.Async.{ async, await }
import play.api.mvc._
import utils._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import traits._
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.cache.Cached
object Publishers extends Controller {
  val searchUtil = new SearchUtils with RemoteClientFromConfig with ResultToJson with UrlFromConfig with BoosFromConfigFile
  def getResourcesByPublisher(publisher: String, page: Option[Int]) = Cached(publisher + page.getOrElse(0)) {
    Action.async {
      async {
        val result = await { searchUtil.searchByPublisher(publisher, page.getOrElse(0)) }
        result match {
          case Some(js) => Ok(js)
          case None     => NotFound
        }
      }
    }
  }
}