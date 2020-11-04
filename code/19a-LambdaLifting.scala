import scala.language.implicitConversions


def map(f: Int => Int, l: List[Int]) : List[Int] = l match {
  case List() => List()
  case x::xs => f(x)::map(f,xs)
}

def addAndMulNToList(n: Int, l: List[Int]) : List[Int] =
  map(y => y*n, map(y => y+n, l))

/** Lambda Lifting */
val f = (n: Int) => (y: Int) => y + n
val g = (n: Int) => (y: Int) => y * n

def addAndMulNToListLL(n: Int, l: List[Int]) : List[Int] =
  map(g(n), map(f(n), l))


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

object CPSTransformed {
  def eval[T](e: Exp, env: Env, k: Value => T) : T = e match {
    case Num(n: Int) => k(NumV(n))
    case Id(x: String) => k(env(x))
    case Add(l: Exp, r: Exp) =>
      eval(l, env, lVal => /* addC1 */
        eval(r, env, rVal => /* addC2 */ (lVal,rVal) match {
        case (NumV(a),NumV(b)) => k(NumV(a+b))
        case _ => sys.error("Can only add numbers")
      }))
    case f@Fun(_,_) => k(ClosureV(f,env))
    case App(f,a) =>
      eval(f, env, fVal => /* appC1 */ fVal match {
        case ClosureV(Fun(p,b),cEnv) => eval(a, cEnv, aVal => /* appC2 */
          eval(b, env+(p -> aVal), k))
        case _ => sys.error("Can only apply functions")
      })
  }
}

object LambdaLifted {
  def addC1[T](r: Exp, env: Env, k: Value => T)(lVal: Value): T =
    eval(r, env, addC2(lVal,k))
  def addC2[T](lVal: Value, k: Value => T)(rVal: Value): T = (lVal,rVal) match {
      case (NumV(a),NumV(b)) => k(NumV(a+b))
      case _ => sys.error("Can only add numbers")
    }
  def appC1[T](a: Exp, env: Env, k: Value => T)(fVal: Value) : T  = fVal match {
      case ClosureV(Fun(p,b),cEnv) => eval(a, cEnv, appC2(b,p,env,k))
      case _ => sys.error("Can only apply functions")
    }
  def appC2[T](b: Exp, p: String, env: Env, k: Value => T)(aVal: Value) : T =
    eval(b, env+(p -> aVal), k)

  def eval[T](e: Exp, env: Env, k: Value => T) : T = e match {
    case Num(n: Int) => k(NumV(n))
    case Id(x: String) => k(env(x))
    case Add(l: Exp, r: Exp) =>
      eval(l, env, addC1(r,env,k))
    case f@Fun(_,_) => k(ClosureV(f,env))
    case App(f,a) =>
      eval(f, env, appC1(a,env,k))
  }
}

val testExp = App(Fun("x",Add("x",1)), Add(1,2))


