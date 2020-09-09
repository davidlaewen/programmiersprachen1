sealed trait Exp
case class Num(n: Int) extends Exp // inherits properties of Scala Int type!
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
}

// examples:
var onePlusTwo = Add(Num(1), Num(2))
assert(eval(onePlusTwo) == 3)
var twoTimesFour = Mul(Num(2), Add(Num(1), Num(3)))
assert(eval(twoTimesFour) == 8)
var threeTimesFourPlusFour = Add(Mul(Num(3),Num(4)), Num(4))
assert(eval(threeTimesFourPlusFour) == 16)
