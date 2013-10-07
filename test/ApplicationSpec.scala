import org.specs2.mutable._
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
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  val client = ElasticClient.remote("localhost", 9300)
  val dbUrl = "http://localhost:5984/standards"
  "Application" should {
    "Search for term" in {
      val result = SearchUtils.searchLR(client)("math", 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
    "Search for bad term" in {
      val result = SearchUtils.searchLR(client)("super bad math", 0, None)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beNone
    }
    "Search for term with accessMode filter" in {
      val term = "visual"
      val result = SearchUtils.searchLR(client)("math", 0, Some(List(term)))
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
      val term = "test"
      val result = SearchUtils.searchLR(client)("math", 0, Some(List(term)))
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
      val content = finalResult.get
      val mediaFeatures = content \\ "mediaFeatures"
      val items = mediaFeatures.map { r =>
        r.as[Seq[String]]
      }
      items.foldLeft(true)((prev, next) => prev && next.contains(term)) must beTrue
    }
    "Search for term with publisher filter" in {
      val term = "Encyclopaedia Britannica, Incorporated"
      val result = SearchUtils.searchLR(client)("time", 0, Some(List(term)))
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
      val result = SearchUtils.standard(client, dbUrl)(standard, 0)
      val finalResult = Await result (result, Duration(2, SECONDS))
      finalResult must beSome[JsValue]
    }
  }
}
