import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

type Env = Map[String, Exp]

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

// define With as syntactic sugar:
def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x,body), xdef)

// interpreter is broken, fix in 5b-Closures.scala
def eval(e: Exp, env: Env) : Exp = e match {
  case Add(l,r) => (eval(l,env),eval(r,env)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l,env),eval(r,env)) match {
    case (Num(a),Num(b)) => Num(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case Id(x) => env(x)
  case App(f,a) => eval(f,env) match {
    case Fun(param, body) =>
      eval(body, Map(param -> eval(a,env))) // call-by-value
      // eval(body, env+(param -> eval(a,env))) // dynamic scoping
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}

val a = wth("b", Num(2), App(Fun("a", Add("a",1)), Add(3,"b")))
assert(eval(a,Map()) == Num(6))
val b = Fun("x","x")
assert(eval(b,Map()) == Fun("x","x"))
val c = App(Fun("x", App(Fun("x",Mul(2,"x")), Add(1,"x"))), 1)
assert(eval(c,Map()) == Num(4))
val d = App(Fun("f", App("f", App("f", 2))), Fun("x",Mul(2,"x"))) // apply double 2 times
assert(eval(d,Map()) == Num(8))

val curry = App( Fun("x", App( Fun("y", Add("x","y")),2)), 3) // causes error!
/**
 * (λx.(λy.x+y 2) 3), Map()
 * ~> (λy.x+y 2), Map(x -> 3)
 * ~> x+y, Map(y -> 2)
 * ~> key not found: x
 */

val ex = wth("x", Num(5), App(Fun("f", App("f",3)), Fun("y", Add("x","y")))) // also causes error
/**
 * With x = 5: (λf.f(3) λy.x+y)
 * ~> (λf.f(3) λy.x+y), Map(x -> 5)
 * ~> (λy.x+y 3)
 * ~> x+y, Map(y -> 3)
 * ~> key not found: x
 */

val ex2 = wth("f", Fun("x", Add("x","y")), wth("y", Num(4), App("f", 1)))
/**
 * Causes no error under dynamic scoping, despite y being free when f is bound.
 * Dynamic scoping can not be used as an alternative to Closures.
 */
