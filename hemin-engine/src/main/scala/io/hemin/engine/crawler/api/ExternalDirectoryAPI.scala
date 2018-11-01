package io.hemin.engine.crawler.api

import java.lang.reflect.Type
import java.util
import java.util.Scanner

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.hemin.engine.util.mapper.UrlMapper

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait ExternalDirectoryAPI {

  def baseUrl: String

  def getFeedUrls(count: Int): List[String]

  protected[this] val gson: Gson = new Gson

  protected[this] def get(url: String): Try[String] =
    UrlMapper.asUrl(url) match {
      case Success(u) => Try {
        new Scanner(u.openStream, "UTF-8")
          .useDelimiter("\\A")
          .next
      }
      case Failure(ex) => Failure(ex) // Note: this is not the same on both sides, one is Try[Option[URL] and the other Try[String
    }

  protected[this] def jsonToMap(json: String): Map[String, AnyRef] = {
    val typ: Type = new TypeToken[util.Map[String, AnyRef]]() {}.getType
    val fromJson: util.Map[String, AnyRef] = gson.fromJson(json, typ)
    fromJson
      .asScala
      .toMap
  }

  protected[this] def jsonToListMap(json: String): List[Map[String, AnyRef]] = {
    val typ: Type = new TypeToken[util.Map[String, AnyRef]]() {}.getType
    val fromJson: util.List[util.Map[String, AnyRef]] = gson.fromJson(json, typ)
    fromJson
      .asScala
      .map(_.asScala.toMap)
      .toList
  }

}
