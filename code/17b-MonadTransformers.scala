import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad[M[_]] {
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]
}

/** Identity Monad */
type Id[X] = X
object IdentityMonad extends Monad[Id] {
  def bind[A,B](x: A, f: A => B) : B = f(x)
  def unit[A](a: A) : A = a
}

/** Option Monad */
object OptionMonad extends Monad[Option] {
  def bind[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
    case Some(y) => f(y)
    case None => None
  }
  def unit[A](a: A) : Option[A] = Some(a)
}
/** List Monad */
object ListMonad extends Monad[List] {
  def bind[A,B](x: List[A], f: A => List[B]): List[B] = x.flatMap(f)
  def unit[A](a: A) : List[A] = List(a)
}

/*
Combining two monads:

- Reader Monad: M[A] = R => A
- State Monad:  M[A] = S => (A,S)
- Reader+State Monad: M[A] = R => S => (A,S)

Could be coded like this:

trait ReaderStateMonad[R,S] extends Monad[({type M[A] = R => S => (A,S)})#M] {
  ...
  def unit[A](a: A) : R => S => (A,S) = r => s => (a,s)
}

But combining monads by hand would require a lot of effort (exponential).
 */

/** Monad Transformers */
// For M[A] and N[A]: Either M[N[A]] or N[M[A]]

type OptionT[M[_]] = { type x[A] = M[Option[A]] }

class OptionTMonad[M[_]](val m: Monad[M]) extends Monad[OptionT[M]#x] { // N[A] = M[Option[A]]
  def unit[A](a: A) : M[Option[A]] = m.unit(Some(a))
  def bind[A,B](x: M[Option[A]], f: A => M[Option[B]]) : M[Option[B]] = {
    m.bind(x, (z: Option[A]) => z match {
      case Some(y) => f(y)
      case None => m.unit(None)
    })}
  def lift[A](x: M[A]) : M[Option[A]] = m.bind(x, (a: A) => m.unit(Some(a)))
}

val OptionMonad2 = new OptionTMonad(IdentityMonad) // combining with IdentityMonad results in the original Monad


val m = new OptionTMonad(ListMonad)
assert(m.bind(List(Some(3),None,Some(4)), (x: Int) => m.unit(x+1)) == List(Some(4),None,Some(5)))
assert(m.bind(List(Some(3),Some(4)), (x: Int) => m.lift(List(1,x))) == List(Some(1),Some(3),Some(1),Some(4)))
assert(m.bind(List(Some(3),Some(4)), (x: Int) => if (x > 3) m.m.unit(None) else m.unit(x*2)) == List(Some(6),None))



