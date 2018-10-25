package io.hemin.engine.model

trait Patchable[T <: Patchable[T]] {

  /**
    * Patches a copy of the current instance with the diff. Every field
    * of the current instance is replaced with the corresponding field
    * of the diff, if the current instance's field is None or Nil.
    *
    * @param diff An instance with the specific fields that get patched
    * @return The copy of this instance with the non-None fields of diff applied
    */
  def patchLeft(diff: T): T

  /**
    * Patches a copy of the current instance with the diff. Every field
    * of the current instance is replaced with the corresponding field
    * of the diff, except when the diff's field field is None or Nil.
    *
    * @param diff An instance with the specific fields that get patched
    * @return The copy of this instance with the non-None fields of diff applied
    */
  def patchRight(diff: T): T

  protected[this] def reduceLeft[A](x: Option[A], y: Option[A]): Option[A] =
    (x, y) match {
      case (None, Some(b)) => y
      case (_, _)          => x
    }

  protected[this] def reduceLeft[A](xs: List[A], ys: List[A]): List[A] =
    (xs, ys) match {
      case (Nil, y::_) => ys
      case (_, _)      => xs
    }

  protected[this] def reduceRight[A](x: Option[A], y: Option[A]): Option[A] =
    (x, y) match {
      case (Some(a), None) => x
      case (_, _)          => y
    }

  protected[this] def reduceRight[A](xs: List[A], ys: List[A]): List[A] =
    (xs, ys) match {
      case (x::_, Nil) => xs
      case (_, _)      => ys
    }

}
