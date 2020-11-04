import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad[M[_]] {
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]
}


/** Operations on Monads */
def fMap[M[_],A,B](f: A => B)(implicit m: Monad[M]) : M[A] => M[B] =
  a => m.bind(a, (x: A) => m.unit(f(x)))

def sequence[M[_],A](l: List[M[A]])(implicit m: Monad[M]) : M[List[A]] = l match {
  case x::xs =>
    m.bind(x, (y: A) =>
      m.bind(sequence(xs), (ys: List[A]) =>
        m.unit(y::ys)))
  case List() => m.unit(List())
}

def mapM[M[_],A,B](f: A => M[B], l: List[A])(implicit m: Monad[M]) : M[List[B]] =
  sequence(l.map(f))

def join[M[_],A](x: M[M[A]])(implicit m: Monad[M]) : M[A] =
  m.bind(x, (y: M[A]) => y)


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

/** Reader Monad ("Environment Passing Style") */
trait ReaderMonad[R] extends Monad[({type M[A] = R => A})#M] { // M[A] = R => A
  def bind[A,B](x: R => A, f: A => R => B) : R => B = r => f(x(r))(r)
  def unit[A](a: A) : R => A = _ => a
}

/** State Monad ("Store Passing Style") */
trait StateMonad[S] extends Monad[({type M[A] = S => (A,S)})#M] {
  def bind[A,B](x: S => (A,S), f: A => S => (B,S)) : S => (B,S) =
    s => x(s) match { case (y,s2) => f(y)(s2) }
  def unit[A](a: A) : S => (A,S) = s => (a,s)
}

/** List Monad */
object ListMonad extends Monad[List] {
  def bind[A,B](x: List[A], f: A => List[B]): List[B] = x.flatMap(f)
  def unit[A](a: A) : List[A] = List(a)
}

/** Continuation Monad */
trait ContinuationMonad[R] extends Monad[({type M[A] = (A => R) => R})#M] { // M[A] = (A => R) => R
  type Cont[X] = (X => R) => R
  def bind[A,B](x: Cont[A], f: A => Cont[B]) : Cont[B] =
    k => x( a => f(a)(k) )
  def unit[A](a: A) : Cont[A] = k => k(a)

  def callCC[A,B](f: (A => Cont[B]) => Cont[A]) : Cont[A] =
    k => f( (a: A) => _ => k(a) )(k)
}

def ex123[R](implicit m: ContinuationMonad[R]) = {
  m.bind(
    m.bind(m.unit(2), (two: Int) =>
    m.bind(m.unit(3), (three: Int) =>
    m.unit(two+three))), (five: Int) =>
      m.unit(1+five))
}

def exCallCC[R](implicit m: ContinuationMonad[R]) = { // (+ 1 (let/cc k (+ 2 (k 3))))
  m.bind(
    m.bind(m.unit(2), (two: Int) =>
    m.callCC[Int,Int]( k => m.bind(k(3), (three: Int) =>
    m.unit(two+three)))), (five: Int) =>
    m.unit(1+five))
}


