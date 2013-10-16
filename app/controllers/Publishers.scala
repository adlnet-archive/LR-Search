package controllers

import play.api.mvc._
import utils._
import traits._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
object Publishers extends Controller with ESClient {
	def getResourcesByPublisher(publisher: String, page: Option[Int]) = Action.async {
	  SearchUtils.searchByPublisher(client)(publisher, page.getOrElse(0)).map{
	    case Some(js) => Ok(js)
	    case None => NotFound
	  }
	}
}