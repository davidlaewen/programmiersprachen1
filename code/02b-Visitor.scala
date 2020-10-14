import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)

type Env = Map[Symbol, Int]

case class Visitor[T](num: Int => T, add: (T,T) => T)

def foldExp[T](v: Visitor[T], e: Exp) : T = e match {
  case Num(n) => v.num(n)
  case Add(l,r) => v.add(foldExp(v,l), foldExp(v,r))
}

val env = Map(Symbol("x") -> 3, Symbol("y") -> 6)

val evalVisitor = new Visitor[Int](n => n, (l,r) => l + r)
val countVisitor = Visitor[Int](n => 1, (l,r) => l + r + 1)
val printVisitor = new Visitor[String](_.toString, _ + " + " + _)

// examples:
val a = Add(2,4)
assert(foldExp(evalVisitor, a) == 6)
assert(foldExp(countVisitor,a) == 3)
assert(foldExp(printVisitor, a) == "2 + 4")
val b = Add(Add(3,4), Add(1,6))
assert(foldExp(evalVisitor, b) == 14)
assert(foldExp(countVisitor, b) == 7)
assert(foldExp(printVisitor, b) == "3 + 4 + 1 + 6")

