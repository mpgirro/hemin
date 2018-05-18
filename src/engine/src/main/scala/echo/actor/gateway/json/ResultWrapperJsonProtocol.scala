package echo.actor.gateway.json

import echo.actor.gateway.json.IndexResultJsonProtocol.IndexResultJsonFormat
import echo.core.domain.dto.{ImmutableResultWrapperDTO, IndexDocDTO, ResultWrapperDTO}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import spray.json.CollectionFormats

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object ResultWrapperJsonProtocol extends DefaultJsonProtocol {
    implicit object ResultWrapperJsonFormat extends RootJsonFormat[ResultWrapperDTO] {
        def write(rw: ResultWrapperDTO) = JsObject(
            "currPage"  -> Option(rw.getCurrPage).map(value => JsNumber(value)).getOrElse(JsNull),
            "maxPage"   -> Option(rw.getMaxPage).map(value => JsNumber(value)).getOrElse(JsNull),
            "totalHits" -> Option(rw.getTotalHits).map(value => JsNumber(value)).getOrElse(JsNull),
            "results"   -> Option(rw.getResults).map(value => JsArray(value.asScala.map(r => IndexResultJsonFormat.write(r)).toVector)).getOrElse(JsNull)
        )
        def read(value: JsValue): ResultWrapperDTO = {
            value.asJsObject.getFields("currPage", "maxPage", "totalHits", "results") match {
                case Seq(JsNumber(currPage), JsNumber(maxPage), JsNumber(totalHits),  JsArray(results)) =>

                    ImmutableResultWrapperDTO.builder()
                        .setCurrPage(currPage.toInt)
                        .setMaxPage(maxPage.toInt)
                        .setTotalHits(totalHits.toInt)
                        .setResults(seqAsJavaList(results.map(_.convertTo[IndexDocDTO]).to[Array]))
                        .create()

                case _ => throw DeserializationException("ResultWrapperDTO expected")
            }
        }
    }
}
