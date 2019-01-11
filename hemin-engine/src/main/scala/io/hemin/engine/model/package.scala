package io.hemin.engine

/** This package provides the domain model classes
  * that the Hemin Engine uses in it's internal subsystems. The external
  * API [[io.hemin.engine.Engine]] also produces results with these model
  * classes.
  *
  * All Engine models are immutable Scala case classes with default falues.
  * Most member variables are either wrapped in an `Option` or are `List`.
  * By default, an `Option` is `None` and a `List` is `Nil`.
  *
  * The models [[io.hemin.engine.model.Podcast]] and [[io.hemin.engine.model.Episode]]
  * are structure more complex internally and hold nested models for additional
  * XML namespaces.
  *
  * All models except the [[io.hemin.engine.model.SearchResult]] extend the
  * [[io.hemin.engine.model.Patchable]] trait. Hence they provide two methods,
  * `patchLeft()` and `patchRight()`, which are used to perform sane updates
  * to the members of a model, with respect to `Option` and `List`. For example,
  * an `Option` that is `Some(a)` will only be patched if the new value is
  * `Some(b)`. Otherwise, the old value will be kept.
  *
  * @author Maximilian Irro
  */
package object model {

}
