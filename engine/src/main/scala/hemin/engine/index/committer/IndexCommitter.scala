package hemin.engine.index.committer

import hemin.engine.model.IndexDoc


trait IndexCommitter {

  def save(doc: IndexDoc): Unit

  def deleteAll(): Unit

}
