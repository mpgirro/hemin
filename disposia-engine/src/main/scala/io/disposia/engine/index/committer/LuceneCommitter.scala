package io.disposia.engine.index.committer

import io.disposia.engine.domain.IndexDoc

class LuceneCommitter extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("LuceneCommitter.save(_) not yet implemented")
  }

}
