package io.disposia.engine.index.committer

import io.disposia.engine.domain.IndexDoc


trait IndexCommitter {

  def save(doc: IndexDoc): Unit

}
