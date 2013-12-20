import org.specs2.specification.After
import org.specs2.specification.Before
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import scala.concurrent.Await
import scala.concurrent.duration._
trait PopulateAndClean {
  val indexName = "lr"
  val documentType = "lr_doc"
  val _client = ElasticClient.remote("localhost", 9300)
  val accessMode = "visual"
  val mediaFeature = "longDescription"
  val key = "DAISY3"
  val publisher = "test publisher"
  def generateMediaFeatures(i: Int): Seq[String] = {
    if (i % 5 == 0) Seq(mediaFeature) else Seq()
  }
  def generateAccessMode(i: Int): Seq[String] = {
    if (i % 3 == 0) Seq(accessMode) else Seq()
  }
  def generateKeys(i: Int): Seq[String] = {
    if (i % 4 == 0) Seq(key) else Seq()
  }
  def generateStandards(i: Int): Seq[String] = {
    if (i % 6 == 0) Seq("s114360a") else Seq()
  }
  def generatePublisher(i: Int) = {
    if (i % 10 == 0) publisher else ""
  }
  def createDocument(i: Int) = {
    Map(
      "title" -> ("title" + i),
      "description" -> ("desc" + i),
      "publisher" -> generatePublisher(i),
      "keys" -> generateKeys(i).toArray,
      "mediaFeatures" -> generateMediaFeatures(i).toArray,
      "accessMode" -> generateAccessMode(i).toArray,
      "standards" -> generateStandards(i).toArray)
  }
  def before = {
    val items = for { i <- 1 until 200 } yield index into s"$indexName/$documentType" id ("8851143037d629a57579139adcf7600" + i) fields (createDocument(i))
    val finalResult = Await result (_client.bulk(items: _*), Duration(2, SECONDS))
  }
  def after = {
    println("after")
    val r = Await result (_client deleteIndex s"$indexName/$documentType", Duration(2, SECONDS))
    println(r.isAcknowledged())
    println(r.toString())
  }
}