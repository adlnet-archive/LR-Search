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
import org.elasticsearch.action.count.CountResponse
@RunWith(classOf[JUnitRunner])
class DataSpec extends Specification {
  val client = ElasticClient.remote("localhost", 9300)
  "Data Utility" should {
    "Get Data for ID" in {
      val testId = "32694a71ab9e3e38507b731670f93c5f"
      val item = DataUtils.doc(client)(testId)
      val doc = Await.result(item, Duration(2, SECONDS))      
      doc must beSome[JsValue]      
            val id = doc.map(x => x \ "_id").map(x => x.as[String])
      id must beSome[String]
      id.get must beEqualTo(testId)
    }
    "Get Data For Invalid Id" in {
      val testId = "Nothing Has This ID"
      val item = DataUtils.doc(client)(testId)
      val doc = Await.result(item, Duration(2, SECONDS))
      doc must beNone      
    }
    "Get Data" in {
      val item = DataUtils.data(client)()
      val targetCount: Long = 0
      val data : CountResponse = Await.result(item, Duration(2, SECONDS))
      data.getCount() must beGreaterThan(targetCount)
    }
    "Get multiple Docs" in {
      val testId = List("32694a71ab9e3e38507b731670f93c5f", "769665cf0106e08b90a09e75cb8c2b8e", "2b23d4c9e18deea9220f5997b8fe4eb1")
      val item = DataUtils.docs(client)(testId)
      val rawDocs = Await.result(item, Duration(2, SECONDS))      
      rawDocs must beSome[JsValue]
      val docs = rawDocs.get
      val count = docs \ "count"
      val resultIds = (docs \\ "_id").map(_.as[String])      
      count.as[Int] must beEqualTo(testId.length)
      resultIds.foldLeft(true)((prev, next) => prev && testId.contains(next)) must beTrue
    }
  }
}