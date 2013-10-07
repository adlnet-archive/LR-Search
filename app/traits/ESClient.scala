package traits
import play.api.Play
import com.sksamuel.elastic4s.ElasticClient
trait ESClient {
	import play.api.Play.current
    val host = Play.application.configuration.getString("es.host").getOrElse("localhost")
    val port = Play.application.configuration.getInt("es.port").getOrElse(9300)
    val client = ElasticClient.remote(host, port)
}