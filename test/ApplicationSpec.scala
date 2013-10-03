package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._
import utils._
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import org.apache.xml.serializer.ToSAXHandler
import com.sksamuel.elastic4s.ElasticClient
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

  "Application" should {

    //    "send 404 on a bad request" in {
    //      running(FakeApplication()) {
    //        route(FakeRequest(GET, "/boum")) must beNone
    //      }
    //    }
    //
    //    "render the index page" in {
    //      running(FakeApplication()) {
    //        val home = route(FakeRequest(GET, "/")).get
    //
    //        status(home) must equalTo(OK)
    //        contentType(home) must beSome.which(_ == "text/html")
    //        contentAsString(home) must contain("Your new application is ready.")
    //      }
    //    }
    "search for results" in {
      val client = ElasticClient.remote("localhost", 9300)
      val results = SearchUtils.searchLR(client)("organic chemistry", 0, None)
      val result = Await.result(results, Duration(1000, MILLISECONDS))
      result must beSome[JsValue]
      client.close()
    }
    "search with accessMode filter" in {
      var client = ElasticClient.remote("localhost", 9300)
      var results = SearchUtils.searchLR(client)("math", 0, Some(List("tactile")))
      val result = Await.result(results, Duration(1000, MILLISECONDS))
      result must beSome[JsValue]
      val jsResult = result.get
      val accessModeValues: Seq[JsValue] = (jsResult \\ "accessMode")
      accessModeValues.map(_.asInstanceOf[JsArray]).foreach { lst =>
        val stringValues: Seq[String] = lst.value.map(_.as[String])
        stringValues.map(_.toLowerCase()).exists(str => str == "tactile") must beTrue
      }
      client.close()
    }
    "search for standards" in {
      val client = ElasticClient.remote("localhost", 9300)
      val url = "http://localhost:5984/standards"
      val results = SearchUtils.standard(client, url)("s11434f5", 0)
      val result = Await.result(results, Duration(1000, MILLISECONDS))
      result must beSome[JsValue]
      client.close()
    }
    "search for invalid standards" in {
      val client = ElasticClient.remote("localhost", 9300)
      val url = "http://localhost:5984/standards"
      val results = SearchUtils.standard(client, url)("abc", 0)
      val result = Await.result(results, Duration(1000, MILLISECONDS))
      result must beNone
      client.close()
    }
    "get document by id" in {
      val client = ElasticClient.remote("localhost", 9300)
      val testId = "769665cf0106e08b90a09e75cb8c2b8e"
      val results = DataUtils.doc(client)(testId)
      val r = Await.result(results, Duration(1000, MILLISECONDS))
      r must beSome[JsValue]
      client.close()
    }
    "get document by invalid id" in {
      val client = ElasticClient.remote("localhost", 9300)
      val testId = "abc"
      val results = DataUtils.doc(client)(testId)
      val r = Await.result(results, Duration(1000, MILLISECONDS))
      r must beNone
      client.close()
    }
    "get count" in {
      val client = ElasticClient.remote("localhost", 9300)
      val results = DataUtils.data(client)()
      val r = Await.result(results, Duration(1000, MILLISECONDS))
      r.getCount() must beGreaterThan(0L)
      client.close()
    }
  }
}
