package utils

import org.elasticsearch.action.get.GetResponse
import play.api.libs.json._
import org.elasticsearch.search.SearchHit
import scala.collection.JavaConversions._
import org.elasticsearch.action.search.SearchResponse
trait ResultToJson {
  def format(docId: String)(resp: Either[SearchHit, GetResponse]): Option[JsValue] = {
    def convertStringList(tmp: java.util.Map[String, Object], field: String): Seq[String] = {
      val rawList = tmp.getOrElse(field, Seq())
      rawList match {
        case scala.collection.immutable.Nil => Seq()
        case _ => rawList.asInstanceOf[java.util.ArrayList[String]].toSeq
      }
    }
    
    def convert(docId: String, tmp: java.util.Map[String, Object]) = {
      Some(Json.toJson(Map(
        "_id" -> Json.toJson(docId),
        "title" -> Json.toJson(tmp.getOrElse("title", "").toString),
        "publisher" -> Json.toJson(tmp.getOrElse("publisher", "").toString),
        "description" -> Json.toJson(tmp.getOrElse("description", "").toString),
        "accessMode" -> Json.toJson(convertStringList(tmp, "accessMode")),
        "mediaFeatures" -> Json.toJson(convertStringList(tmp, "mediaFeatures")),
        "hasScreenshot" -> Json.toJson(true),
        "url" -> Json.toJson(tmp.getOrElse("url", "").toString))))
    }
    
    resp match {
      case Left(r) => { convert(r.getId, r.getSource()) }
      case Right(r) => { if (r.isExists()) convert(docId, r.getSource()) else None }
    }
  }
  def format(hits: SearchResponse): Option[JsValue] = {
    if (hits.getHits().totalHits() > 0) {
      Some(Json.toJson(Map(
        "count" -> Json.toJson(hits.getHits().totalHits()),
        "data" -> Json.toJson(hits.getHits().getHits().map(x => format("")(Left(x)))))))
    } else {
      None
    }
  }
}