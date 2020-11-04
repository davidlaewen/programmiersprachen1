import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: Symbol) extends Exp
case class With(x: Symbol, xdef: Exp, body: Exp) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(Symbol(s))
def sym(s: String) : Symbol = Symbol(s)

def subst(body: Exp, i: Symbol, v: Num) : Exp = body match {
  case Num(_) => body
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else body
  case With(x,xdef,body) =>  // do not substitute in body if x is redefined
    With(x, subst(xdef,i,v), if (x == i) body else subst(body,i,v))
}

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x.name)
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
  case With(x,xdef,body) => eval(subst(body,x,Num(eval(xdef))))
    // evaluation occurs before substitution, call-by-value
}

val a = Add(5, With(sym("x"), 7, Add("x", 3)))
assert(eval(a) == 15)
val b = With(sym("x"), 1, With(sym("x"), 2, Mul("x","x")))
assert(eval(b) == 4)
val c = With(sym("x"), 5, Add("x", With(sym("x"), 3, "x")))
assert(eval(c) == 8)
val d = With(sym("x"), 1, With(sym("x"), Add("x",1), "x"))
assert(eval(d) == 2)
/*
with x = 1:
  with y = x: y
 */


