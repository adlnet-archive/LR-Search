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
class ApplicationSpec extends Specification {
  val client = ElasticClient.local
  for (i <- 1 until 5) {
    client.sync.execute {
      index into "lr/lr_doc" id ("8851143037d629a57579139adcf7600" + i) fields (
        "title" -> ("title" + i),
        "description" -> ("desc" + i),
        "publisher" -> "",
        "keys" -> List(for (j <- 1 until 3) yield ("key" + j)),
        "mediaFeatures" -> List("longDescription"),
        "accessMode" -> List("visual"),
        "keys" -> List(),
        "standards" -> List()
      )
    }
  }
  for (i <- 6 until 10) {
    client.sync.execute {
      index into "lr/lr_doc" id ("8851143037d629a57579139adcf7600" + i) fields (
        "title" -> ("title" + i),
        "description" -> ("desc" + i),
        "publisher" -> "",
        "keys" -> List(for (j <- 1 until 3) yield ("key" + j)),
        "mediaFeatures" -> List("longDescription"),
        "accessMode" -> List(),
        "keys" -> List("DAISY3", "Primary Doc"),
        "standards" -> List()
      )
    }
  }
  for (i <- 11 until 15) {
    client.sync.execute {
      index into "lr/lr_doc" id ("8851143037d629a57579139adcf7600" + i) fields (
        "title" -> ("title" + i),
        "description" -> ("desc" + i),
        "publisher" -> "",
        "keys" -> List(for (j <- 1 until 3) yield ("key" + j)),
        "mediaFeatures" -> List("longDescription"),
        "accessMode" -> List(),
        "keys" -> List(),
        "standards" -> List("s114360a")
      )
    }
  }
  for (i <- 16 until 20) {
    client.sync.execute {
      index into "lr/lr_doc" id ("8851143037d629a57579139adcf7600" + i) fields (
        "title" -> ("title" + i),
        "description" -> ("desc" + i),
        "publisher" -> "Encyclopaedia Britannica, Incorporated",
        "keys" -> List(for (j <- 1 until 3) yield ("key" + j)),
        "mediaFeatures" -> List("longDescription"),
        "accessMode" -> List(),
        "keys" -> List(),
        "standards" -> List()
      )
    }
  }      
  val dbUrl = "http://localhost:5984/standards"
  val boost: SearchBoosts = SearchBoosts(5, 4, 3, 2)
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
