package exo.engine.catalog.mongo

import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class PodcastMongoRepository (db: Future[DefaultDB])
                             (implicit ec: ExecutionContext) {

    //private def collection: BSONCollection = db.collection("podcasts")
    private def collection: Future[BSONCollection] = db.map(_.collection("podcasts"))

}
