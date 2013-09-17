package utils
import dispatch._
import Defaults._
import java.io.InputStream
object StandardsUtil {
  def standards(): Future[InputStream] = {
    val std = url("http://localhost:5984/standards/_design/standards/_list/just-keys/all?reduce=false")
    val resp = Http(std)
    for {
      d <- resp
    } yield d.getResponseBodyAsStream()
  }
  def getStandard(standardId: String): Future[InputStream] = {
    val std = url("http://localhost:5984/standards") / standardId
    val resp = Http(std)
    for {
      d <- resp
    } yield d.getResponseBodyAsStream()
  }
}