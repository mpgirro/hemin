package io.hemin.engine.index.committer

import io.hemin.engine.domain.IndexDoc


trait IndexCommitter {

  def save(doc: IndexDoc): Unit

}
