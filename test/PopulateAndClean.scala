import org.specs2.specification.After
import org.specs2.specification.Before
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import scala.concurrent.Await
import scala.concurrent.duration._
trait PopulateAndClean {
  val indexName = "lr/lr_doc"
  val client = ElasticClient.remote("localhost", 9300)
  def generateMediaFeatures(i: Int) = {
    if (i % 5 == 0) List("longDescription")
    else List()
  }
  def generateAccessMode(i: Int) = {
    if (i % 3 == 0) List("visual")
    else List()
  }
  def generateKeys(i: Int) = {
    if (i % 4 == 0) List(for (j <- 1 until i) yield ("key" + j))
    else List()
  }
  def generateStandards(i: Int) = {
    if (i % 6 == 0) List("s114360a")
    else List()
  }
  def createDocument(i: Int) = {
    Map(
      "title" -> ("title" + i),
      "description" -> ("desc" + i),
      "publisher" -> "",
      "keys" -> generateKeys(i),
      "mediaFeatures" -> generateMediaFeatures(i),
      "accessMode" -> generateAccessMode(i),
      "standards" -> generateStandards(i))
  }
  def before = {
    val items = for { i <- 1 until 20 } yield index into indexName id ("8851143037d629a57579139adcf7600" + i) fields (createDocument(i))
    val finalResult = Await result (client.bulk(items: _*), Duration(2, SECONDS))
  }
  def after = {
    Await result (client deleteIndex indexName, Duration(2, SECONDS))
  }
}