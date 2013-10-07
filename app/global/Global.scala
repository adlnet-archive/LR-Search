import play.api.GlobalSettings
import filters._
import play.api.mvc.WithFilters
object Global extends WithFilters(CorsFilter) {
}