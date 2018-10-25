package io.hemin.engine.index.committer

import io.hemin.engine.model.IndexDoc


trait IndexCommitter {

  def save(doc: IndexDoc): Unit

}
