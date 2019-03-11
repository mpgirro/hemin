package hemin.engine.index.committer

import hemin.engine.model.IndexDoc

class LuceneCommitter
  extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = throw new UnsupportedOperationException("LuceneCommitter.save(_) not yet implemented")

  override def deleteAll(): Unit = throw new UnsupportedOperationException("LuceneCommitter.deleteAll(_) not yet implemented")

}
