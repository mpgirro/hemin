package hemin.engine.index

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import hemin.engine.{HeminConfig, HeminEngine}
import hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/** Configuration for [[hemin.engine.index.IndexStore]] */
final case class IndexConfig (
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
) extends ConfigStandardValues {
  override val namespace: String = IndexConfig.namespace
}

object IndexConfig
  extends ConfigDefaults[IndexConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminConfig.namespace}.${IndexStore.name}"

  override def fromConfig(config: Config): IndexConfig =
    IndexConfig(
      solrUri         = config.getString(s"$namespace.solr-uri"),
      solrQueueSize   = config.getInt(s"$namespace.solr-queue-size"),
      solrThreadCount = config.getInt(s"$namespace.solr-thread-count"),
      createIndex     = config.getBoolean(s"$namespace.create-index"),
      commitInterval  = config.getInt(s"$namespace.commit-interval").seconds,
      workerCount     = config.getInt(s"$namespace.handler-count"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.solr-uri"          -> s"http://localhost:8983/solr/${HeminEngine.name}",
    s"$namespace.solr-queue-size"   -> 20,
    s"$namespace.solr-thread-count" -> 4,
    s"$namespace.create-index"      -> false,
    s"$namespace.commit-interval"   -> 3,
    s"$namespace.handler-count"     -> 5,
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
      mailbox-type = "${classOf[IndexMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
