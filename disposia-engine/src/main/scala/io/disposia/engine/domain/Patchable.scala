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

}
