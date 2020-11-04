import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
// case class ClosureV(f: Fun, env: Env) extends Value
case class FunV(f: Value => Value) extends Value

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


def eval(e: Exp) : Env => Value = e match {
  case Num(n) => _ => NumV(n)
  case Id(x) => env => env(x)
  case Add(l,r) => env => (eval(l)(env),eval(r)(env)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Fun(p,b) => env => FunV( v => eval(b)(env+(p -> v)))
  case App(f,a) => env => (eval(f)(env),eval(a)(env)) match {
    case (FunV(g),arg) => g(arg)
    case _ => sys.error("Can only apply functions")
  }
}