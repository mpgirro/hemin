package io.disposia.engine.index.committer

import io.disposia.engine.domain.IndexDoc
import io.disposia.engine.olddomain.OldIndexDoc


trait IndexCommitter {

  def save(doc: IndexDoc): Unit

}
