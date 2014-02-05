package traits

import play.api.Play

trait UrlContainer {
	def dbUrl: String
	def standardsUrl: String
}

trait UrlFromConfig extends UrlContainer{
  import play.api.Play.current
  def dbUrl: String = Play.application.configuration.getString("couchdb.db.metadata.url").getOrElse("http://localhost:5984/lr-data")
  def standardsUrl: String = Play.application.configuration.getString("couchdb.db.url").getOrElse("http://localhost:5984/standards")
}