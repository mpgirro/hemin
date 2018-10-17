package api.v1.utils

// Required to protect against JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
case class ArrayWrapper[T](results: Iterable[T])
