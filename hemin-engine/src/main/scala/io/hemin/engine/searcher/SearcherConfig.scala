package io.hemin.engine.searcher

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) extends ConfigStandardValues {
  override def configPath: String = SearcherConfig.configPath
}

object SearcherConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override def configPath: String = "hemin.searcher"

  override protected[this] def defaultValues: Config = ConfigFactory.parseMap(Map(
    configPath+".solr-uri"     -> "http://localhost:8983/solr/hemin",
    configPath+".default-page" -> 1,
    configPath+".default-size" -> 20,
  ).asJava)

  override protected[this] def defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] def defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[SearcherPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
