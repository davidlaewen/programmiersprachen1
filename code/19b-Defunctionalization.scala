import scala.language.implicitConversions

/*
def map(f: Int => Int, l: List[Int]) : List[Int] = l match {
  case List() => List()
  case x::xs => f(x)::map(f,xs)
}


def addAndMulNToList(n: Int, l: List[Int]) : List[Int] =
  map(y => y*n, map(y => y+n, l))
 */

/** After lambda lifting */
val f = (n: Int) => (y: Int) => y + n
val g = (n: Int) => (y: Int) => y * n

/*
def addAndMulNToListLL(n: Int, l: List[Int]) : List[Int] =
  map(g(n), map(f(n), l))
 */

/** Represent closure using data container class */
sealed abstract class FunctionValue
case class F(n: Int) extends FunctionValue
case class G(n: Int) extends FunctionValue

def apply(f: FunctionValue, y: Int) : Int = f match {
  case F(n) => y + n
  case G(n) => y * n
}

def map(f: FunctionValue, l: List[Int]) : List[Int] = l match {
  case List() => List()
  case x::xs => apply(f,x)::map(f,xs)
}

def addAndMulNToList(n: Int, l: List[Int]) : List[Int] =
  map(G(n), map(F(n), l))


/** CPS-interpreter */
sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

object Defunctionalized {
  sealed abstract class FunctionValue[T]
  case class AddC1[T](r: Exp, env: Env, k: FunctionValue[T]) extends FunctionValue[T]
  case class AddC2[T](lVal: Value, k: FunctionValue[T]) extends FunctionValue[T]
  case class AppC1[T](a: Exp, env: Env, k: FunctionValue[T]) extends FunctionValue[T]
  case class AppC2[T](b: Exp, p: String, env: Env, k: FunctionValue[T]) extends FunctionValue[T]
  case class Out[T]() extends FunctionValue[T] // for returning results
  case class End[T]() extends FunctionValue[T] // "trap" state, machine cannot be terminated

  def apply[T](fv: FunctionValue[T], v: Value) : T = fv match {
    case AddC1(r,env,k) => eval(r, env, AddC2(v,k))
    case AddC2(lVal,k) => (lVal,v) match {
      case (NumV(a),NumV(b)) => apply(k, NumV(a+b))
      case _ => sys.error("Can only add numbers")
    }
    case AppC1(a,env,k) => v match {
      case ClosureV(Fun(p,b),cEnv) => eval(a, cEnv, AppC2(b,p,env,k))
      case _ => sys.error("Can only apply functions")
    }
    case AppC2(b,p,env,k) => eval(b, env+(p -> v), k)
    case Out() => print(v); apply(End(), v)
    case End() => apply(End(), v)
  }

  def eval[T](e: Exp, env: Env, k: FunctionValue[T]) : T = e match {
    case Num(n: Int) => apply(k, NumV(n))
    case Id(x: String) => apply(k, env(x))
    case Add(l: Exp, r: Exp) =>
      eval(l, env, AddC1(r,env,k))
    case f@Fun(_,_) => apply(k,ClosureV(f,env))
    case App(f,a) =>
      eval(f, env, AppC1(a,env,k))
  }
}

val testExp = App(Fun("x",Add("x",1)), Add(1,2))

