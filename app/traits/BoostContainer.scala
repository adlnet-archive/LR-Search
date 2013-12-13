package traits

import utils.SearchBoosts
import play.api.Play

trait BoostContainer {
  def boost: SearchBoosts
}

trait BoosFromConfigFile extends BoostContainer{
  import Play.current
  val titlePhraseBoost = Play.application.configuration.getInt("search.title.phrase.boost").getOrElse(5)
  val descriptionPhraseBoot = Play.application.configuration.getInt("search.desc.phrase.boost").getOrElse(4)
  val titleBoost = Play.application.configuration.getInt("search.title.boost").getOrElse(3)
  val descriptionBoost = Play.application.configuration.getInt("search.desc.boost").getOrElse(2)
  val url = Play.application.configuration.getString("couch.db.url").getOrElse("http://localhost:5984/standards")
  def boost: SearchBoosts = SearchBoosts(titlePhraseBoost, titleBoost, descriptionPhraseBoot, descriptionPhraseBoot)
}