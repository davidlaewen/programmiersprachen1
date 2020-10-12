import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad[M[_]] {
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]
}

implicit def monadicSyntax[A, M[_]](m: M[A])(implicit mm: Monad[M]) : Object = new {
  def map[B](f: A => B): Any = mm.bind(m, (x: A) => mm.unit(f(x)))
  def flatMap[B](f: A => M[B]): M[B] = mm.bind(m, f)
}

/** Option Monad */
object OptionMonad extends Monad[Option] {
  override def bind[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
    case Some(y) => f(y)
    case None => None
  }
  override def unit[A](a: A) : Option[A] = Some(a)
}
/* Monad laws for Option Monad:

bind(unit(x),f) == bind(Some(x),f) == f(x)

bind(x, unit) == Some(y) | for x == Some(y)
bind(x, unit)  == None | for x == None

bind(bind(x,f),g) == bind(f(y),g) == bind(x, y => bind(f(y),g)) | for x == Some(y)
bind(bind(x,f),g) == None == bind(x, y => bind(f(y),g)) | for x == None
 */

/** Operations on Monads */
def fMap[M[_],A,B](f: A => B)(implicit m: Monad[M]) : M[A] => M[B] =
  a => m.bind(a, (x: A) => m.unit(f(x)))

val f = fMap((x: Int) => x+1)(OptionMonad)
assert(f(Some(1)) == Some(2))
assert(f(None) == None)

def sequence[M[_],A](l: List[M[A]])(implicit m: Monad[M]) : M[List[A]] = l match {
  case x::xs =>
    m.bind(x, (y: A) =>
    m.bind(sequence(xs), (ys: List[A]) =>
    m.unit(y::ys)))
  case List() => m.unit(List())
}

assert(sequence[Option,Int](List(Some(1),None,Some(2)))(OptionMonad) == None)
assert(sequence[Option,Int](List(Some(1),Some(2),Some(3)))(OptionMonad) == Some(List(1,2,3)))

def mapM[M[_],A,B](f: A => M[B], l: List[A])(implicit m: Monad[M]) : M[List[B]] =
  sequence(l.map(f))

def join[M[_],A](x: M[M[A]])(implicit m: Monad[M]) : M[A] =
  m.bind(x, (y: M[A]) => y)

assert(join[Option,Int](Some(Some(5)))(OptionMonad) == Some(5))
assert(join[Option,Int](Some(None))(OptionMonad) == None)

/** Identity Monad */
type Id[X] = X
object IdentityMonad extends Monad[Id] {
  def bind[A,B](x: A, f: A => B) : B = f(x)
  def unit[A](a: A) : A = a
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





