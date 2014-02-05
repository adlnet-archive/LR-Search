package controllers
import scala.async.Async.{ async, await }
import play.api.mvc._
import utils._
import traits._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
object Publishers extends Controller {
  
  def getResourcesByPublisher(publisher: String, page: Option[Int]) = Action.async {
    async {
      val searchUtil = new SearchUtils with RemoteClientFromConfig with ResultToJson with UrlFromConfig with BoosFromConfigFile
      val result = await { searchUtil.searchByPublisher(publisher, page.getOrElse(0)) }
      searchUtil.client.close
      result match {
        case Some(js) => Ok(js)
        case None => NotFound
      }
    }
  }
}