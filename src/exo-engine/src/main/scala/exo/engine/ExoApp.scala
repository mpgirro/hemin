package exo.engine

import akka.util.Timeout
import com.typesafe.scalalogging.Logger

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object ExoApp {

    private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

    private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

    private val log = Logger(classOf[App])

    def main(args: Array[String]): Unit = {
        val engine = new ExoEngine()

        println("Starting engine...")
        engine.start()
        Thread.sleep(10000) // let the actorRefs propagate

        println("Proposing feed...")
        engine.propose("https://feeds.metaebene.me/freakshow/m4a")
        Thread.sleep(10000)

        println("Starting search...")
        engine
            .search("hukl", 1, 20)
            .onComplete {
                case Success(results) =>
                    println("Search results:")
                    for (r <- results.getResults.asScala)
                        println(r.getTitle+"\n")
                case Failure(reason)  => println("ERROR: " + reason.getMessage)
            }

    }

}
