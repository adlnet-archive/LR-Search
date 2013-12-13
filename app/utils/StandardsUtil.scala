package utils
import dispatch._
import Defaults._
import java.io.InputStream
import traits.UrlContainer
class StandardsUtil {
  this: UrlContainer =>
  def standards(): Future[InputStream] = {
    val std = url(dbUrl) / "_design" / "standards" / "_list" / "just-keys" / "all" <<? Map("reduce" -> "false")
    val resp = Http(std)
    resp.map(d => d.getResponseBodyAsStream())
  }
  def getStandard(standardId: String): Future[InputStream] = {
    val std = url(dbUrl) / standardId
    val resp = Http(std)
    resp.map(d => d.getResponseBodyAsStream())
  }
}