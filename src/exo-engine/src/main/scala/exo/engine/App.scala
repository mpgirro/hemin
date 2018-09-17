package exo.engine

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

object App {

    private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

    def main(args: Array[String]): Unit = {
        val engine = new ExoEngine()

        println("Starting engine...")
        engine.start()

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
    }

}
