import org.specs2.specification.After
import org.specs2.specification.Before
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import scala.concurrent.Await
import scala.concurrent.duration._
trait PopulateAndClean {
  val indexName = "lr/lr_doc"
  val client = ElasticClient.remote("localhost", 9300)
  val accessMode = "visual"
  val mediaFeature = "longDescription"
  val key = "DAISY3"
  val publisher = "test publisher"
  def generateMediaFeatures(i: Int): String = {
    if (i % 5 == 0) mediaFeature else ""
  }
  def generateAccessMode(i: Int): String = {
    if (i % 3 == 0) accessMode else ""
  }
  def generateKeys(i: Int): String = {
    if (i % 4 == 0) key else ""
  }
  def generateStandards(i: Int): String = {
    if (i % 6 == 0) "s114360a" else ""
  }
  def generatePublisher(i: Int) = {
    if (i % 10 == 0) publisher else ""
  }
  def createDocument(i: Int) = {
    Map(
      "title" -> ("title" + i),
      "description" -> ("desc" + i),
      "publisher" -> generatePublisher(i),
      "keys" -> generateKeys(i),
      "mediaFeatures" -> generateMediaFeatures(i),
      "accessMode" -> generateAccessMode(i),
      "standards" -> generateStandards(i))
  }
  def before = {
    println("before")
    val items = for { i <- 1 until 200 } yield index into indexName id ("8851143037d629a57579139adcf7600" + i) fields (createDocument(i))
    val finalResult = Await result (client.bulk(items: _*), Duration(2, SECONDS))
  }
  def after = {
    println("after")
    val r = Await result (client deleteIndex indexName, Duration(2, SECONDS))
    println(r.isAcknowledged())
    println(r.toString())
  }
}