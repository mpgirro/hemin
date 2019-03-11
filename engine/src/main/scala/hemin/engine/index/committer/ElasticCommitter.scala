package hemin.engine.index.committer

import hemin.engine.model.IndexDoc

class ElasticCommitter
  extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = throw new UnsupportedOperationException("ElasticCommitter.save(_) not yet implemented")

  override def deleteAll(): Unit = throw new UnsupportedOperationException("ElasticCommitter.deleteAll(_) not yet implemented")

}
