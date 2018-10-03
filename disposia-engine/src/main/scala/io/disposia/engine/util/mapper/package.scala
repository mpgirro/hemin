package io.disposia.engine.util

package object mapper {

  def reduce[A](x: Option[A], y: Option[A]): Option[A] =
    (x, y) match {
      case (Some(a), _)    => Some(a)
      case (None, Some(b)) => Some(b)
      case (None,  None)   => None
    }

  def reduce[A](x: List[A], y: List[A]): List[A] = {
    (x, y) match {
      case (List(), List())   => List()
      case (List(xs), List()) => x
      case (_, List(ys))      => y
    }
  }

}
