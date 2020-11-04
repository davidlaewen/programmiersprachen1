import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

case class Shift(param: String, body: Exp) extends Exp
case class Reset(body: Exp) extends Exp

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value
case class ContV(k: Value => Value) extends Value

def eval(e: Exp, env: Env, k: Value => Value) : Value = e match {
  case Num(n) => k(NumV(n))
  case Id(x) => k(env(x))
  case Add(l,r) => eval(l, env, lv => eval(r, env, rv => (lv,rv) match {
    case (NumV(a),NumV(b)) => k(NumV(a+b))
    case _ => sys.error("Can only add numbers")
  }))
  case f@Fun(_,_) => k(ClosureV(f,env))
  case App(f,a) => eval(f, env, fv => fv match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(a, env, av => eval(b, cEnv+(p -> av), k))
    case ContV(k2) => eval(a, env, av => k(k2(av))) // apply continuation k after k2 (composition)
    case _ => sys.error("Can only apply functions")
  })
  case Reset(e) => k(eval(e,env,x=>x)) // reset current continuation to identity function
  case Shift(p,b) => eval(b, env+(p -> ContV(k)), x=>x) // add binding to env, reset current continuation
}


def startEval(e: Exp) : Value = {
  eval(e,Map(),x=>x) // pass identity function as initial continuation
}

val test = Add(100, Reset(Add(1, Shift("k", App("k", App("k",5))))))
assert(eval(test,Map(),x=>x) == NumV(107))




