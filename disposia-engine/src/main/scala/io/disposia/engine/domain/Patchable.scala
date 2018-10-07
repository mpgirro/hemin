package io.disposia.engine.domain

trait Patchable[T <: Patchable[T]] {

  /**
    * Patches a copy of the current instance with the diff . Only non-None fields
    * of the diff overwrite the values of the current instance in the copy.
    *
    * @param diff An instance with the specific fields that get patched
    * @return The copy of this instance with the non-None fields of diff applied
    */
  def patch(diff: T): T

  protected[this] def reduce[A](x: Option[A], y: Option[A]): Option[A] =
    (x, y) match {
      case (Some(a), _)    => Some(a)
      case (None, Some(b)) => Some(b)
      case (None,  None)   => None
    }

  protected[this] def reduce[A](xs: List[A], ys: List[A]): List[A] = {
    (xs, ys) match {
      case (List(), List())   => List()
      case (List(as), List()) => xs
      case (_, List(bs))      => ys
    }
  }

}
