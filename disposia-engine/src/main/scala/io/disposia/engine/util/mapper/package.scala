package io.disposia.engine.util

package object mapper {

  def reduce[A](x: Option[A], y: Option[A]): Option[A] =
    (x, y) match {
      case (Some(a), _)    => Some(a)
      case (None, Some(b)) => Some(b)
      case (None,  None)   => None
    }

  def reduce[A](xs: List[A], ys: List[A]): List[A] = {
    (xs, ys) match {
      case (List(), List())   => List()
      case (List(as), List()) => xs
      case (_, List(bs))      => ys
    }
  }

}
