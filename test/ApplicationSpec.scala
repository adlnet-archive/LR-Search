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
        val results = SearchUtils.searchLR("organic chemistry", 0)
        val result = Await.result(results, Duration(1000, MILLISECONDS))
        result.getHits.totalHits() must beGreaterThan(0L)
    }
    "search for standards" in {
        val results = SearchUtils.standard("s1005356", 0)
        val result = Await.result(results, Duration(1000, MILLISECONDS))
        result.getHits.totalHits() must beGreaterThan(0L)
    }
    "search for invalid standards" in {
        val results = SearchUtils.standard("abc", 0)
        val result = Await.result(results, Duration(1000, MILLISECONDS))
        result.getHits.totalHits() must beEqualTo(0L)
    }    
    "get document by id" in {
    	val testId = "769665cf0106e08b90a09e75cb8c2b8e"	
    	val results = SearchUtils.getDoc(testId)	
    	val r = Await.result(results, Duration(1000, MILLISECONDS))
    	r.isExists() must beTrue
    	r.getId() must beEqualTo(testId).ignoreCase
    }
    "get document by invalid id" in {
    	val testId = "abc"	
    	val results = SearchUtils.getDoc(testId)	
    	val r = Await.result(results, Duration(1000, MILLISECONDS))
    	r.isExists() must beFalse
    }    
  }
}
