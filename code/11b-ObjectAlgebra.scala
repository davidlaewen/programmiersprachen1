/** Object Algebra style */
trait NumSig[T] {
  def z: T
  def s(p: T) : T
}

trait Num { def apply[T](x: NumSig[T]) : T}
