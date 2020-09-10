import language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp

implicit def num2exp(n: Int) = Num(n)

val a = Add(1,Add(2,3))
val b = Num(1)
val c = Add(Add(Add(Add(1,1),1),1),1)

def count(e: Exp) : Int = e match {
  case Num(_) => 1
  case Add(l,r) => count(l) + count(r)
}

assert(count(a) == 3)
assert(count(b) == 1)
assert(count(c) == 5)

def print(e: Exp) : String = e match {
  case Num(n) => n.toString
  case Add(l,r) => "("+print(l)+"+"+print(r)+")"
}

assert(print(a) == "(1+(2+3))")
assert(print(b) == "1")
assert(print(c) == "((((1+1)+1)+1)+1)")