package io.hemin.engine.index.committer

import io.hemin.engine.model.IndexDoc

class LuceneCommitter extends IndexCommitter {

  override def save(doc: IndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("LuceneCommitter.save(_) not yet implemented")
  }

}
