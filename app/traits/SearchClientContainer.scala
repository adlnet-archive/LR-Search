package traits
import play.api.Play
import com.sksamuel.elastic4s.ElasticClient

trait SearchClientContainer {
  def indexName: String
  def documentType: String
  def client: ElasticClient
}
trait RemoteClientFromConfig extends SearchClientContainer {
  import play.api.Play.current
  val host = Play.application.configuration.getString("es.host").getOrElse("localhost")
  val port = Play.application.configuration.getInt("es.port").getOrElse(9300)
  def client: ElasticClient = ElasticClient.remote(host, port)
  def indexName: String = "lr"
  def documentType: String = "lr_doc"    
}