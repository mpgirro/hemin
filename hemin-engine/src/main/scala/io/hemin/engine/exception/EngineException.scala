package io.hemin.engine.exception

object EngineException {
  /** Allows to access the message and the cause via pattern-matching */
  def unapply(e: EngineException): Option[(String,Throwable)] = Some((e.getMessage, e.getCause))
}

/**
  *
  * @see The class design follows the recommendation in this post [[https://stackoverflow.com/a/43942163/9812594]]
  */
class EngineException (message: String)
  extends Exception(message) {

  def this(message: String, cause: Throwable) = {
    this(message)
    initCause(cause)
  }

  def this(cause: Throwable) = this(Option(cause).map(_.toString).orNull, cause)

  def this() = this(null: String)

}
