import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Fun(f: Exp => Exp) extends Exp // higher-order abstract syntax (HOAS)
case class App(f: Exp, a: Exp) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)


def eval(e: Exp) : Exp = e match {
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(f) => eval(f(eval(a)))
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}


// val a = App(Fun("x", Add("x",1)), 5) // first-order syntax
val a = App(Fun(x => Add(x,1)),5) // higher-order syntax
assert(eval(a) == Num(6))






