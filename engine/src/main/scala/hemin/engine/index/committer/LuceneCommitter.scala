package hemin.engine.index.committer

import hemin.engine.model.IndexDoc

/** This Committer is currently unimplemented. For a future version,
  * it is intended to send data to a Lucene index.
  */
class LuceneCommitter
  extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = throw new UnsupportedOperationException("LuceneCommitter.save(_) not yet implemented")

  override def deleteAll(): Unit = throw new UnsupportedOperationException("LuceneCommitter.deleteAll(_) not yet implemented")

}
