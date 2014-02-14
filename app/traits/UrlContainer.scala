package traits

import play.api.Play

trait UrlContainer {
  def dbUrl: String
  def standardsUrl: String
}

trait UrlFromConfig extends UrlContainer {
  import play.api.Play.current
  lazy val dbUrl: String = Play.application.configuration.getString("couchdb.db.metadata.url").getOrElse("http://localhost:5984/lr-data")
  lazy val standardsUrl: String = Play.application.configuration.getString("couchdb.db.url").getOrElse("http://localhost:5984/standards")
}