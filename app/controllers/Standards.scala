package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import utils._
import play.api.libs.iteratee.Enumerator

object Standards extends Controller {
  def standards() = Action { request =>
	  val std = StandardsUtil.standards()
	  Async {
	    std.map { data =>
	      val resp = Enumerator.fromStream(data)
	      Ok.stream(resp).withHeaders("Content-Type" -> "application/json")
	    }
	  }    
  }
	def standard(standardId: String) =  Action  { request =>
	  val std = StandardsUtil.getStandard(standardId)
	  Async {
	    std.map { data =>
	      val resp = Enumerator.fromStream(data)
	      Ok.stream(resp).withHeaders("Content-Type" -> "application/json")
	    }
	  }
	}
}