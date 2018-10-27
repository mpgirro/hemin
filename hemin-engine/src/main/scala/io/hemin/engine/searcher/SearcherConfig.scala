package io.hemin.engine.searcher

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.Engine
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) extends ConfigStandardValues {
  override val configPath: String = SearcherConfig.configPath
}

object SearcherConfig
  extends ConfigDefaults[SearcherConfig]
    with ConfigStandardValues {

  override val configPath: String = s"${Engine.name}.${Searcher.name}"

  override def fromConfig(config: Config): SearcherConfig =
    SearcherConfig(
      solrUri     = config.getString(s"$configPath.solr-uri"),
      defaultPage = config.getInt(s"$configPath.default-page"),
      defaultSize = config.getInt(s"$configPath.default-size"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.solr-uri" -> s"http://localhost:8983/solr/${Engine.name}",
    s"$configPath.default-page" -> 1,
    s"$configPath.default-size" -> 20,
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
      mailbox-type = "${classOf[SearcherPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
