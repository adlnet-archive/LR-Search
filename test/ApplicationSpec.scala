import org.specs2.mutable._
import scala.math._
import utils._
import org.specs2.runner._
import org.junit.runner._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import play.api.test._
import play.api.test.Helpers._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.libs.json._
import play.Logger
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with After {
  val dbUrl = "http://localhost:5984/standards"
  val boost: SearchBoosts = SearchBoosts(5, 4, 3, 2)
  val client = ElasticClient.local
  val indexName = "lr/lr_doc"
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
  for (i <- 1 until 20) {
    client.sync.execute {
      index into indexName id ("8851143037d629a57579139adcf7600" + i) fields (createDocument(i))
    }
  }

  def after = {
    val result = client.deleteIndex(indexName)
    val finalResult = Await result (result, Duration(2, SECONDS))
    println(finalResult.isAcknowledged())
  }
  "Application" should {
    "Search for term" in {
      val result = SearchUtils.searchLR(client, dbUrl, boost)("title1", 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search for bad term" in {
      val result = SearchUtils.searchLR(client, dbUrl, boost)("aaaabbbbccaeged", 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beNone
    }
    "Search for term with accessMode filter" in {
      val term = "visual"
      val result = SearchUtils.searchLR(client, dbUrl, boost)("title1", 0, Some(List(term)))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val accessModes = content \\ "accessMode"
      val items = accessModes.map { r =>
        r.as[Seq[String]]
      }
      items.foldLeft(true)((prev, next) => prev && next.contains(term)) must beTrue
    }
    "Search for term with mediaFeatures filter" in {
      val term = "longDescription"
      val result = SearchUtils.searchLR(client, dbUrl, boost)("title11", 0, Some(List(term)))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val mediaFeatures = content \\ "mediaFeatures"
      val items = mediaFeatures.map { r =>
        r.as[Seq[String]]
      }
      items.foldLeft(true)((prev, next) => prev && next.contains(term)) must beTrue
    }
    "Search for term with key filter" in {
      val result = SearchUtils.searchLR(client, dbUrl, boost)("title6", 0, Some(List("DAISY3")))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val keys = content \\ "keys"
      val items = keys.map { r =>
        r.as[String]
      }
      items.forall(x => x == "DAISY3") must beTrue
    }
    "Search for term with multiple filter" in {
      val terms = List("Primary Doc", "DAISY3")
      val result = SearchUtils.searchLR(client, dbUrl, boost)("title6", 0, Some(terms))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search for term with publisher filter" in {
      val term = "Encyclopaedia Britannica, Incorporated"
      val result = SearchUtils.searchLR(client, dbUrl, boost)("time", 0, Some(List(term)))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val publisher = content \\ "publisher"
      val items = publisher.map { r =>
        r.as[String]
      }
      items.forall(x => x == term) must beTrue
    }
    "Search for Standards" in {
      val standard = "s114360a"
      val result = SearchUtils.searchLR(client, dbUrl, boost)(standard, 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search for Standards Regular Term" in {
      val standard = "test1"
      val result = SearchUtils.searchLR(client, dbUrl, boost)(standard, 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search by publisher" in {
      def minimum(i1: Int, i2: Int, i3: Int) = min(min(i1, i2), i3)
      def distance(s1: String, s2: String) = {
        val dist = Array.tabulate(s2.length + 1, s1.length + 1) { (j, i) => if (j == 0) i else if (i == 0) j else 0 }
        for (j <- 1 to s2.length; i <- 1 to s1.length)
          dist(j)(i) = if (s2.charAt(j - 1) == s1.charAt(i - 1)) dist(j - 1)(i - 1)
          else minimum(dist(j - 1)(i) + 1, dist(j)(i - 1) + 1, dist(j - 1)(i - 1) + 1)
        dist(s2.length)(s1.length)
      }
      val publisher = "Encyclopaedia Britannica, Incorporated"
      val result = SearchUtils.searchByPublisher(client)(publisher, 0)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val publishers = content \\ "publisher"
      val items = publishers.map { r =>
        distance(r.as[String], publisher)
      }
      //the list works if the Levenshtein distance is at most one change
      items.foldLeft(true)((prev: Boolean, next: Int) => prev && next <= 1) must beTrue
    }
  }
}
