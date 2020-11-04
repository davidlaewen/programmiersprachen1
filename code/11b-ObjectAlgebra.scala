/** Object Algebra style: Numbers */
trait NumSig[T] { // functor
  def s(p: T) : T
  def z: T
}

trait Num { def apply[T](x: NumSig[T]) : T} //functor algebra

def plus(a: Num, b: Num) : Num = new Num {
  def apply[T](x: NumSig[T]) : T = a.apply(new NumSig[T] {
    def s(p: T): T = x.s(p)
    def z: T = b.apply(x)
  })
}

val zero: Num = new Num { def apply[T](x: NumSig[T]): T = x.z }
val one: Num = new Num { def apply[T](x: NumSig[T]): T = x.s(x.z) }
val two: Num = new Num { def apply[T](x: NumSig[T]) : T = x.s(one.apply(x))}
val three: Num = new Num { def apply[T](x: NumSig[T]): T = x.s(two.apply(x))}


object NumAlg extends NumSig[Int] {
  def s(x: Int) : Int = x+1
  def z = 0
}

assert(plus(two,three)(NumAlg) == 5)

/** Object Algebra Style: Lists */
trait IntListSig[T] {
  def cons(h: Int, rest: T) : T
  def empty : T
}

trait IntList { def apply[T](x: IntListSig[T]) : T}
