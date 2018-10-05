package io.disposia.engine.index.committer

import io.disposia.engine.domain.IndexDoc

class ElasticCommitter extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticCommitter.save(_) not yet implemented")
  }

}
