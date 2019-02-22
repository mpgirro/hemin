package hemin.engine

import com.github.simplyscala.MongodProps
import com.typesafe.config.{Config, ConfigFactory}
import de.flapdoodle.embed.mongo.config.{MongoCmdOptionsBuilder, MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.config.IRuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import hemin.engine.catalog.repository.RepositoryFactory

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool


object MongoTestContext {
  lazy val defaultMongoHost: String = "localhost"
  lazy val defaultMongoPort: Int = 12345
}

/** This class allows to use an embedded mongo DB in a test setup
  * without implementing the `com.github.simplyscala.MongoEmbedDatabase`
  * trait, which does not allow sufficient support for database
  * configuration (we had to enable journaling for the database for
  * ReactiveMongo).
  *
  */
class MongoTestContext {

  private val executionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(8))

  private val mongoProps: MongodProps = {
    val runtimeConfig: IRuntimeConfig = new RuntimeConfigBuilder()
      .defaults(Command.MongoD)
      .processOutput(ProcessOutput.getDefaultInstanceSilent)
      .build()

    def runtime(config: IRuntimeConfig): MongodStarter = MongodStarter.getInstance(config)

    val mongodExe: MongodExecutable = runtime(runtimeConfig).prepare(
      new MongodConfigBuilder()
        .version(Version.V3_5_1) // TODO: in production we use Mongo v4.x
        .net(new Net(MongoTestContext.defaultMongoPort, Network.localhostIsIPv6()))
        .cmdOptions(new MongoCmdOptionsBuilder()
          .useNoJournal(false)
          .build())
        .build()
    )
    MongodProps(mongodExe.start(), mongodExe)
  }

  /** Stops the mongod process */
  def stop(): Unit = {
    Option(mongoProps).foreach(_.mongodProcess.stop())
    Option(mongoProps).foreach(_.mongodExe.stop())
  }

  lazy val mongoHost: String = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getServerAddress)
    .map(_.getCanonicalHostName)
    .getOrElse(MongoTestContext.defaultMongoHost)

  lazy val mongoPort: Int = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getPort)
    .getOrElse(MongoTestContext.defaultMongoPort)

  lazy val mongoUri: String = s"mongodb://$mongoHost:$mongoPort/${HeminEngine.name}"

  /** The Config object from which the database connection parameters are taken */
  lazy val config: Config = ConfigFactory
    .parseMap(Map(
      "hemin.engine.catalog.mongo-uri" -> mongoUri,
    ).asJava)
    .withFallback(HeminConfig.defaultConfig)

  /** The typesafe engine config variant from which the database connection parameters are taken */
  lazy val engineConfig: HeminConfig = HeminConfig.load(config)

  lazy val repositoryFactory: RepositoryFactory = new RepositoryFactory(engineConfig.catalog, executionContext)

}
