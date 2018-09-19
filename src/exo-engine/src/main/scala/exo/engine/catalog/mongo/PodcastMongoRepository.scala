package exo.engine.catalog.mongo

import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class PodcastMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    //private def collection: BSONCollection = db.collection("podcasts")
    private def collection: BSONCollection = db.collection("podcasts")

}
