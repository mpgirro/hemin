package exo.engine

import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import exo.engine.catalog.CatalogProtocol.ProposeNewFeed
import exo.engine.index.IndexProtocol.{IndexResultsFound, SearchIndex}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

object App {

    private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

    private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

    private val log = Logger(classOf[App])

    def main(args: Array[String]): Unit = {
        val engine = new ExoEngine()

        println("Starting engine...")
        engine.start()

        println("Proposing feed...")
        engine.bus() ! ProposeNewFeed("https://feeds.metaebene.me/freakshow/m4a")

        Thread.sleep(5000)

        println("Starting search...")
        /*
        (engine.bus() ? SearchIndex("hukl", 1, 20)).onComplete {
            case Success(IndexResultsFound(_,results)) =>
                println("Search results:")
                for (r <- results.getResults.asScala)
                    println(r.getTitle+"\n")
            case Failure(reason)  => println("ERROR: " + reason.getMessage)
        }
        */

        val future = engine.bus() ? SearchIndex("hukl", 1, 20)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[IndexResultsFound]
        response match {
            case IndexResultsFound(_,results)  =>
                println("Search results:")
                for (r <- results.getResults.asScala)
                    println(r.getTitle+"\n")
            case other => log.error("Received unexpected result message type type : {}", other.getClass)
        }

        /*
        val catalog = engine.catalogBroker()
        val index = engine.indexBroker()

        println("Proposing feed...")
        engine.propose("https://feeds.metaebene.me/freakshow/m4a")

        Thread.sleep(5000)

        println("Starting search...")
        val f = engine.search("hukl", 1, 20)
        f onComplete {
            case Success(results) =>
                println("Search results:")
                for (r <- results.getResults.asScala)
                    println(r.getTitle+"\n")
            case Failure(reason)  => println("ERROR: " + reason.getMessage)
        }
        */
    }

}
