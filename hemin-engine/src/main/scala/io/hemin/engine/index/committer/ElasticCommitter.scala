package io.hemin.engine.index.committer

import io.hemin.engine.model.IndexDoc

class ElasticCommitter
  extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticCommitter.save(_) not yet implemented")
  }

}
