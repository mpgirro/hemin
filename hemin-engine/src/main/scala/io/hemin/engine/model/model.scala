package io.hemin.engine.model

/** The [[io.hemin.engine.model]] package provides the model classes
  * that the Hemin Engine uses in it's internal subsystems. The external
  * API [[io.hemin.engine.Engine]] also produces results with these model
  * classes.
  *
  * All Engine models are immutable Scala case classes with default falues.
  * Most member variables are either wrapped in an `Option` or are `List`.
  * By default, an `Option` is `None` and a `List` is `Nil`.
  *
  * The models [[io.hemin.engine.model.Podcast]] and [[io.hemin.engine.model.Episode]]
  * are structure more complex internally. The [[io.hemin.engine.model.info]]
  * package provides the nested information classes.
  *
  * All models except the [[io.hemin.engine.model.ResultPage]] extend the
  * [[io.hemin.engine.model.Patchable]] trait. Hence they provide two methods,
  * [[io.hemin.engine.model.Patchable.patchLeft()]] and [[io.hemin.engine.model.Patchable.patchRight()]],
  * which are used to perform sane updates to the members of a model, with
  * respect to `Option` and `List`. For example, and `Option` that is `Some(a)`
  * will only be patched if the new value is `Some(b)`. Otherwise, the old value
  * will be kept.
  *
  * @author Maximilian Irro
  */
object model {

}
