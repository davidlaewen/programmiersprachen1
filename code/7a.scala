import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
case class If0(cond: Exp, thenExpr: Exp, elseExpr: Exp) extends Exp
case class Letrec(x: String, xDef: Exp, body: Exp) extends Exp

object Values {
  trait ValueHolder {
    def value: Value
  }
  sealed abstract class Value extends ValueHolder {
    def value : Value = this
  }
  case class ValuePointer(var v: Value) extends ValueHolder {
    def value : Value = v
  }
  case class NumV(n: Int) extends Value
  case class ClosureV(f: Fun, env: Env) extends Value
  type Env = Map[String,ValueHolder]
}

import Values._

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


def eval(e: Exp, env: Env) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => env(x).value
  case Add(l,r) => (eval(l,env),eval(r,env)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l,env),eval(r,env)) match {
    case (NumV(a),NumV(b)) => NumV(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case f@Fun(_,_) => ClosureV(f,env)
  case App(f,a) => eval(f,env) match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(b, cEnv+(p -> eval(a,env))) // call-by-value
    case _ => sys.error("Can only apply functions")
  }
  case If0(c,t,f) => eval(c,env) match {
    case NumV(0) => eval(t,env)
    case NumV(_) => eval(f,env)
    case _ => sys.error("Can only check if number is zero")
  }
  case Letrec(i,v,b) =>
    val vp = ValuePointer(null)
    val newEnv = env+(i -> vp)
    vp.v = eval(v,newEnv)
    eval(b,newEnv)
}

val facAttempt = wth("fac", Fun("n", If0("n", 1, Mul("n", App("fac", Add("n",-1))))), App("fac",4))
/** Evaluation causes error, since usage of "fun" identifier in xDef is not in scope of "fun" binding.
 * We require a new language construct (Letrec) to implement recursive binding of identifiers:
 */
val fac = Letrec("fac", Fun("n", If0("n", 1, Mul("n", App("fac", Add("n",-1))))), App("fac",4))