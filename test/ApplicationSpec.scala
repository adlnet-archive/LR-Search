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
import scala.util.Try
import org.elasticsearch.action.count.CountResponse
import traits._
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification with After with Before with PopulateAndClean {
  val _dbUrl = "http://localhost:5984/standards"
  "Search Should" should {
    val searchUtils = new SearchUtils with SearchClientContainer with UrlContainer with BoostContainer with ResultToJson {
      def boost = SearchBoosts(5, 4, 3, 2)
      def dbUrl = _dbUrl
      def standardsUrl = "http://localhost:5984/standards"
      def client = _client
      val indexName = currentIndexName
      val documentType = currentDocumentType
    }
    "Search for term" in {
      val result = searchUtils.searchLR("title1", 0, None, None, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search for bad term" in {
      val result = searchUtils.searchLR("aaaabbbbccaeged", 0, None, None, None)
      val finalResult = Await result (result, duration)
      finalResult must beNone
    }
    "Search for term with accessMode filter" in {
      val result = searchUtils.searchLR("title3", 0, None, None, Some(List(accessMode)))
      val finalResult = Await result (result, duration)
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val accessModes = content \\ "accessMode"
      val items = accessModes.map { r =>
        r.as[Seq[String]]
      }
      items.foldLeft(true)((prev, next) => prev && next.contains(accessMode)) must beTrue
    }
    "Search for term with key filter" in {
      val result = searchUtils.searchLR("title4", 0, Some(List(key)), None, None)
      val finalResult = Await result (result, duration)
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val keys = content \\ "keys"
      val items = keys.map { r =>
        r.as[String]
      }
      items.forall(x => x == key) must beTrue
    }
    "Search for term with multiple filter" in {
      val terms = List("visual", "DAISY3")
      val result = searchUtils.searchLR("title12", 0, Some(List("DAISY3")), None, Some(List("visual")))
      val finalResult = Await result (result, duration)
      finalResult must beSome[JsValue]
    }
    "Search for video" in {
      val result = searchUtils.searchLR("title12", 0, None, Some("video"), None)
      val finalResult = Await result (result, duration)
      finalResult must beSome[JsValue]
    }
    "Search for term with publisher filter" in {
      val term = "test publisher"
      val result = searchUtils.searchLR("title10", 0, Some(List(term)), None, None)
      val finalResult = Await result (result, duration)
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
      val result = searchUtils.searchLR(standard, 0, None, None, None)
      val finalResult = Await result (result, duration)
      finalResult must beSome[JsValue]
    }
    "Search for Standards Regular Term" in {
      val standard = "title1"
      val result = searchUtils.searchLR(standard, 0, None, None, None)
      val finalResult = Await result (result, duration)
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
      val result = searchUtils.searchByPublisher(publisher, 0)
      val finalResult = Await result (result, duration)
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
  "Data util" should {
    val dataUtils = new DataUtils with SearchClientContainer with UrlContainer with BoostContainer with ResultToJson {
      def boost = SearchBoosts(5, 4, 3, 2)
      def dbUrl = _dbUrl
      def standardsUrl = "http://localhost:5984/standards"
      def client = _client
      val indexName = currentIndexName
      val documentType = currentDocumentType
    }
    "Get Data for ID" in {
      val testId = "8851143037d629a57579139adcf76001"
      val item = dataUtils.doc(testId)
      val doc = Await.result(item, duration)
      doc must beSome[JsValue]
      val id = doc.map(x => x \ "_id").map(x => x.as[String])
      id must beSome[String]
      id.get must beEqualTo(testId)
    }
    "Get Data For Invalid Id" in {
      val testId = "Nothing Has This ID"
      val item = dataUtils.doc(testId)
      val doc = Await.result(item, duration)
      doc must beNone
    }
    "Get Data" in {
      val item = dataUtils.data()
      val targetCount: Long = 0
      val data: CountResponse = Await.result(item, duration)
      data.getCount() must beGreaterThan(targetCount)
    }
    "Get multiple Docs" in {
      val testId = for (i <- 1 until 3) yield "8851143037d629a57579139adcf7600" + i
      val item = dataUtils.docs(testId)
      val rawDocs = Await.result(item, duration)
      rawDocs must beSome[JsValue]
      val docs = rawDocs.get
      val count = docs \ "count"
      val resultIds = (docs \\ "_id").map(_.as[String])
      count.as[Int] must beEqualTo(testId.length)
      resultIds.foldLeft(true)((prev, next) => prev && testId.contains(next)) must beTrue
    }
  }
}
