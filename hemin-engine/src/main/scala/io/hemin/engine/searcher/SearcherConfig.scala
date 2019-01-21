package io.hemin.engine.searcher

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.HeminEngine
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) extends ConfigStandardValues {
  override val namespace: String = SearcherConfig.namespace
}

object SearcherConfig
  extends ConfigDefaults[SearcherConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminEngine.name}.${Searcher.name}"

  override def fromConfig(config: Config): SearcherConfig =
    SearcherConfig(
      solrUri     = config.getString(s"$namespace.solr-uri"),
      defaultPage = config.getInt(s"$namespace.default-page"),
      defaultSize = config.getInt(s"$namespace.default-size"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.solr-uri" -> s"http://localhost:8983/solr/${HeminEngine.name}",
    s"$namespace.default-page" -> 1,
    s"$namespace.default-size" -> 20,
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
