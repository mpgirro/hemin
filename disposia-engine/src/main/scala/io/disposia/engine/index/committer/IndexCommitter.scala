package io.disposia.engine.index.committer

import io.disposia.engine.newdomain.NewIndexDoc
import io.disposia.engine.olddomain.OldIndexDoc


trait IndexCommitter {

  def save(doc: NewIndexDoc): Unit

}
