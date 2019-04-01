package hemin.engine.index.committer

import hemin.engine.model.Document

/** This Committer is currently unimplemented. For a future version,
  * it is intended to send data to an ElasticSearch index.
  */
class ElasticCommitter
  extends IndexCommitter {

  override def save(doc: Document): Unit = throw new UnsupportedOperationException("ElasticCommitter.save(_) not yet implemented")

  override def deleteAll(): Unit = throw new UnsupportedOperationException("ElasticCommitter.deleteAll(_) not yet implemented")

}
