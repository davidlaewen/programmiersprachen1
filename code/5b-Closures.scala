import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


def eval(e: Exp, env: Env) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => env(x)
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
    case ClosureV(f,cEnv) =>
      eval(f.body, cEnv+(f.param -> eval(a,env))) // call-by-value
    case _ => sys.error("Can only apply functions")
  }
}


val a = wth("b", Num(2), App(Fun("a", Add("a",1)), Add(3,"b")))
assert(eval(a,Map()) == NumV(6))
val b = Fun("x","x")
assert(eval(b,Map()) == ClosureV(Fun("x","x"),Map()))
val c = App(Fun("x", App(Fun("x",Mul(2,"x")), Add(1,"x"))), 1)
assert(eval(c,Map()) == NumV(4))
val d = App(Fun("f", App("f", App("f", 2))), Fun("x",Mul(2,"x"))) // apply double 2 times
assert(eval(d,Map()) == NumV(8))
val curry = App( Fun("x", App( Fun("y", Add("x","y")), 2)), 3)
assert(eval(curry,Map()) == NumV(5))
/**
 * (λx.(λy.x+y 2) 3), Map() - Closure(λx.(λy.x+y 2), Map())
 * ~> (λy.x+y 2), Map(x -> 3) - Closure(λy.x+y, Map(x -> 3))
 * ~> x+y, Map(x -> 3, y -> 2)
 * ~> 5
 */

val ex = wth("x", Num(5), App( Fun("f", App("f",3)), Fun("y", Add("x","y")) ))
assert(eval(ex,Map()) == NumV(8))
/**
 * With x = 5: (λf.(f 3) λy.x+y), Map()
 * ~> (λf.(f 3) λy.x+y), Map(x -> 5) - Closure(λf.(f 3), Map(x -> 5))
 * ~> (f 3), Map(x -> 5, f -> λy.x+y) - Closure(λy.x+y, Map(x -> 5, f -> λy.x+y)
 * ~> x+y, Map(x -> 5, f -> λy.x+y, y -> 3)
 * ~> 8
 */