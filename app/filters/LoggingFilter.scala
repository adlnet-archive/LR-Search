import play.api.mvc._
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
object LoggingFilter extends Filter {
  def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {
    val start = System.currentTimeMillis

      def logTime(result: SimpleResult): SimpleResult = {
        val time = System.currentTimeMillis - start
        result.header.status match {
          case 404 => Logger.warn(s"${rh.method} ${rh.uri} took ${time}ms and returned ${result.header.status}")
          case 500 => Logger.error(s"${rh.method} ${rh.uri} took ${time}ms and returned ${result.header.status}")
          case _   => Logger.info(s"${rh.method} ${rh.uri} took ${time}ms and returned ${result.header.status}")
        }

        result.withHeaders("Request-Time" -> time.toString)
      }
    next(rh).map(logTime)
  }
}