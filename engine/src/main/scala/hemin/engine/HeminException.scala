package hemin.engine

object HeminException {
  /** Allows to access the message and the cause via pattern-matching */
  def unapply(e: HeminException): Option[(String,Throwable)] = Some((e.getMessage, e.getCause))
}

/**
  *
  * @see The class design follows the recommendations from this
  *      post [[https://stackoverflow.com/a/43942163/9812594]]
  */
class HeminException(message: String)
  extends Exception(message) {

  def this(message: String, cause: Throwable) = {
    this(message)
    initCause(cause)
  }

  def this(cause: Throwable) = this(Option(cause).map(_.toString).orNull, cause)

  def this() = this(null: String)

}
