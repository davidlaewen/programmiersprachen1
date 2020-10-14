import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad {
  type M[_] // abstract type member instead of abstract type parameter
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]

  implicit def monadicSyntax[A](m: M[A]) = new {
    def map[B](f: A => B) : M[B] = bind(m, (x: A) => unit(f(x)))
    def flatMap[B](f: A => M[B]) : M[B] = bind(m,f)
  }
}

trait ReaderMonad extends Monad {
  type R
  def ask: M[R]
  def local[A](f: R => R, a: M[A]) : M[A]
}

trait ReaderMonadImpl extends ReaderMonad {
  type M[X] = R => X
  def unit[A](a: A) : M[A] = r => a
  def bind[A,B](m: M[A], f: A => M[B]) : M[B] = r => f(m(r))(r)
  def ask : M[R] = r => r
  def local[A](f: R => R, a: M[A]) : M[A] = r => a(f(r))
}

object ReaderExample extends ReaderMonadImpl {
  type R = Int
  def ex1 : M[Int] = for {x <- ask} yield (x+1)
  def ex2 : M[Int] = local(_ => 99, ex1)
}

object ReaderExample2 extends ReaderMonadImpl {

  trait Exp
  case class Id(x: String) extends Exp
  case class And(l: Exp, r: Exp) extends Exp
  case class Or(l: Exp, r: Exp) extends Exp

  implicit def string2exp(s: String) : Exp = Id(s)

  type R = Map[String,Boolean]

  def eval(e: Exp) : M[Boolean] = e match {
    case Id(x) => for {env <- ask} yield env(x)
    case And(l,r) => for {
      x <- eval(l)
      y <- eval(r)
    } yield x&&y
    case Or(l,r) => for {
      x <- eval(l)
      y <- eval(r)
    } yield x||y
  }

}


trait StateMonad extends Monad {
  type S
  def getState : M[S]
  def putState(s: S) : M[Unit]
}

trait StateMonadImpl extends StateMonad {
  type M[A] = S => (A,S)
  def bind[A,B](m: M[A], f: A => M[B]) : M[B] = (s: S) => {
    val (a,s2) = m(s)
    f(a)(s2)
  }
  def unit[A](a: A) : M[A] = (s: S) => (a,s)
  def getState : M[S] = s => (s,s)
  def putState(s: S) : M[Unit] = sOld => ((),s)
}

trait ContinuationMonad extends Monad {
  def callCC[A,B](f: (A => M[B]) => M[A]) : M[A]
}



