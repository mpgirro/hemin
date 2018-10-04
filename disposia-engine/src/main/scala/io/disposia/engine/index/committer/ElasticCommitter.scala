package io.disposia.engine.index.committer

import io.disposia.engine.newdomain.NewIndexDoc

class ElasticCommitter extends IndexCommitter {

  override def save(doc: NewIndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticCommitter.save(_) not yet implemented")
  }

}
