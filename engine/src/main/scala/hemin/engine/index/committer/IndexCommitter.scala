package hemin.engine.index.committer

import hemin.engine.model.Document

trait IndexCommitter {

  /** Save the document to the reverse index structure. If a document with
    * the ID is known to the Index, the respective document will be updated.
    * Otherwise, the document will be added.
    *
    * @param doc The document to save.
    */
  def save(doc: Document): Unit

  /** Deletes all documents from the reverse index structure */
  def deleteAll(): Unit

}
