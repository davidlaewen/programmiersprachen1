import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: Symbol) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(Symbol(s))
def sym(s: String) : Symbol = Symbol(s)

// type definition, 'Env' is alias for type on rhs
type Env = Map[Symbol, Int]

def eval(e: Exp, env: Env) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l,env) + eval(r,env)
  case Mul(l,r) => eval(l,env) * eval(r,env)
  case Id(x) => env(x)
}

// examples
val env = Map(sym("x") -> 2, sym("y") -> 4)
val a = Add(Mul("x",5), Mul("y",7))
assert(eval(a, env) == 38)
val b = Mul(Mul("x", "x"), Add("x", "x"))
assert(eval(b, env) == 16)



