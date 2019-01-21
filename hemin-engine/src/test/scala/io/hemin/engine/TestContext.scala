package io.hemin.engine

import com.github.simplyscala.MongodProps
import com.typesafe.config.{Config, ConfigFactory}
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.process.config.IRuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import io.hemin.engine.catalog.repository.RepositoryFactory

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool

class TestContext(mongoProps: MongodProps) {

  private val executionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(8))

  val mongoHost: String = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getServerAddress)
    .map(_.getCanonicalHostName)
    .getOrElse("localhost")

  val mongoPort: Int = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getPort)
    .getOrElse(12345)

  val mongoUri: String = s"mongodb://$mongoHost:$mongoPort/${HeminEngine.name}"

  private val runtimeConfig: IRuntimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build()

  val config: Config = ConfigFactory
    .parseMap(Map(
      "hemin.catalog.mongo-uri" -> mongoUri,
    ).asJava)
    .withFallback(HeminConfig.defaultConfig)

  val engineConfig: HeminConfig = HeminConfig.load(config)

  val repositoryFactory: RepositoryFactory = new RepositoryFactory(engineConfig.catalog, executionContext)

}
