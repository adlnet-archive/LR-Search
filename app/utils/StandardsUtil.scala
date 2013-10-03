package utils
import dispatch._
import Defaults._
import java.io.InputStream
object StandardsUtil {
  def standards(dbUrl: String)(): Future[InputStream] = {
    val std = url(dbUrl) / "_design" / "standards" / "_list" / "just-keys" / "all" <<? Map("reduce" -> "false")
    val resp = Http(std)
    resp.map(d => d.getResponseBodyAsStream())
  }
  def getStandard(dbUrl: String)(standardId: String): Future[InputStream] = {
    val std = url(dbUrl) / standardId
    val resp = Http(std)
    resp.map(d => d.getResponseBodyAsStream())
  }
}