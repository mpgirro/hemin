package hemin.engine.graph

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import hemin.engine.HeminConfig
import hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

final case class GraphConfig (
  neo4jUri: String,
  username: String,
  password: String,
  createGraph: Boolean,
) extends ConfigStandardValues {
  override val namespace: String = GraphConfig.namespace
}

object GraphConfig
  extends ConfigDefaults[GraphConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminConfig.namespace}.${GraphStore.name}"

  override def fromConfig(config: Config): GraphConfig =
    GraphConfig(
      neo4jUri    = config.getString(s"$namespace.neo4j-uri"),
      username    = config.getString(s"$namespace.username"),
      password    = config.getString(s"$namespace.password"),
      createGraph = config.getBoolean(s"$namespace.create-graph"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.neo4j-uri"    -> "bolt://localhost/7687",
    s"$namespace.username"     -> "",
    s"$namespace.password"     -> "",
    s"$namespace.create-graph" -> true,
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
      mailbox-type = "${classOf[GraphPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))


}
