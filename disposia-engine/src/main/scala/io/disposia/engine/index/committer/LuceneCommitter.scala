package io.disposia.engine.index.committer

import io.disposia.engine.newdomain.NewIndexDoc

class LuceneCommitter extends IndexCommitter {

  override def save(doc: NewIndexDoc): Unit = {
    // TODO implement!
    throw new UnsupportedOperationException("LuceneCommitter.save(_) not yet implemented")
  }

}
