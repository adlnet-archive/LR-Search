package utils
import scala.async.Async.{ async, await }
import dispatch._
import Defaults._
import java.io.InputStream
import traits.UrlContainer
class StandardsUtil {
  this: UrlContainer =>
  def standards(): Future[InputStream] = {
    async {
      val std = url(standardsUrl) / "_design" / "standards" / "_list" / "just-keys" / "all" <<? Map("reduce" -> "false")
      val resp = await { Http(std) }
      resp.getResponseBodyAsStream()
    }
  }
  def getStandard(standardId: String): Future[InputStream] = {
    async {
      val std = url(standardsUrl) / standardId
      val resp = await { Http(std) }
      resp.getResponseBodyAsStream()
    }
  }
}