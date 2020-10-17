import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(name: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

type Value = Int
type Env = Map[String, Value]
type R = Env

type M[X] = R => X
def unit[A](a: A): M[A] =
  r => a
def bind[A, B](action1: M[A])(action2: A => M[B]): M[B] =
  r => action2(action1(r))(r)
def ask: M[R] =
  r => r
def local[A](f: R => R, p: M[A]): M[A] =
  r => p(f(r))

// This code adds methods map and flatMap on values of type M[A].
implicit class monadicSyntax[A](p: M[A]) {
  def flatMap[B](f: A => M[B]): M[B] = bind(p)(f)
  def map[B](f: A => B): M[B] = flatMap(x => unit(f(x)))
}

object OriginalInterpreterVersion {
  def eval(e: Exp): M[Value] = e match {
    case Num(n) =>
      unit(n)
    case Id(x) =>
      for {
        env <- ask
      } yield env(x)
    case Add(l, r) =>
      for {
        lV <- eval(l)
        rV <- eval(r)
      } yield lV + rV
  }
}

object InterpreterWithDesugaredForComprehensions {
  def eval(e: Exp): M[Value] = e match {
    case Num(n) =>
      unit(n)
    case Id(x) =>
      ask.map { env =>
        env(x)
      }
    case Add(l, r) =>
      eval(l).flatMap( lV =>
        eval(r).map( rV =>
          lV+rV ))
  }
}

object InlineMapFlatMap {
  def eval(e: Exp): M[Value] = e match {
    case Num(n) =>
      unit(n)
    case Id(x) =>
      //First, inline map:
      /** ask.flatMap(env =>
            unit(env(x))) */
      //Then, inline flatMap:
      bind(ask) { env =>
        unit(env(x))
      }
    case Add(l, r) =>
      // Inline map:
      /** bind(eval(l))( lV =>
            eval(r).flatMap( rV =>
              unit(lV+rV))) */
      // Inline flatMap:
      bind(eval(l))( lV =>
        bind(eval(r))( rV =>
          unit(lV+rV)))
  }
}

object InlineBindUnitAsk {
  def eval(e: Exp): M[Value] = e match {
    case Num(n) =>
      r => n
    case Id(x) =>
      r => ( (env: Env) =>
        (r: R) => env(x))(((r: R) => r)(r))(r)
    case Add(le, re) =>
      r => ( (lV: Value) =>
        (r: R) => ( (rV: Value) =>
          (r: R) => lV+rV)(eval(re)(r))(r))(eval(le)(r))(r)
  }
}

object FinalSimplifiedInterpreter {
  def eval(e: Exp): Env => Value = e match {
    case Num(n) =>
      r =>
        n
    case Id(x) =>
      (env: Env) => env(x)
    case Add(l, r) =>
      env => eval(l)(env) + eval(r)(env)
  }
}

// example expressions for testing
val ex1 = Add(1,2)
val ex2 = Add("x",Add(1,"y"))
val map = Map("x" -> 4, "y" -> 5)
