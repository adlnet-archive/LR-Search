package controllers
import scala.async.Async.{ async, await }
import play.api.mvc._
import utils._
import traits._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
object Publishers extends Controller {
  val searchUtil = new SearchUtils with RemoteClientFromConfig with ResultToJson with UrlFromConfig with BoosFromConfigFile
  def getResourcesByPublisher(publisher: String, page: Option[Int]) = Action.async {
    async {
      await { searchUtil.searchByPublisher(publisher, page.getOrElse(0)) } match {
        case Some(js) => Ok(js)
        case None => NotFound
      }
    }
  }
}