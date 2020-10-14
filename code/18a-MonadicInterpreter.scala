import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad {
  type M[_]
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]

  implicit def monadicSyntax[A](m: M[A]): Object = new {
    def map[B](f: A => B) : M[B] = bind(m, (x: A) => unit(f(x)))
    def flatMap[B](f: A => M[B]) : M[B] = bind(m,f)
  }
}

trait ReaderMonad extends Monad {
  type R
  def ask: M[R]
  def local[A](f: R => R, a: M[A]) : M[A]
}


