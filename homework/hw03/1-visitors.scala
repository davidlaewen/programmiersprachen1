import scala.language.implicitConversions

object AEId {

  sealed abstract class Exp

  case class Num(n: Int) extends Exp

  case class Add(lhs: Exp, rhs: Exp) extends Exp

  case class Mul(lhs: Exp, rhs: Exp) extends Exp

  case class Id(x: Symbol) extends Exp

  implicit def num2exp(n: Int) = Num(n)
  implicit def sym2exp(x: Symbol) = Id(x)

  type Env = Map[Symbol, Int]

  def eval(e: Exp, env: Env): Int = e match {
    case Num(n) => n
    case Id(x) => env(x)
    case Add(l, r) => eval(l, env) + eval(r, env)
    case Mul(l, r) => eval(l, env) * eval(r, env)
  }

  def count(e: Exp): Int = e match {
    case Num(n) => 1
    case Id(x) => 0
    case Add(l, r) => count(l) + count(r)
    case Mul(l, r) => count(l) + count(r)
  }

  def print(e: Exp): String = e match {
    case Num(n) => n.toString
    case Id(x) => x.name
    case Add(l, r) => "(" + print(l) + "+" + print(r) + ")"
    case Mul(l, r) => print(l) + "*" + print(r)
  }

  val testEnv = Map('x -> 3, 'y -> 4)

  val test0 = Add(Mul('x, 2), Add('y, 'y))
  val test1 = Add(Mul(21, 2), Add('x, Mul(5, 3)))
  val test2 = Mul(Add('x, 'y), Add(1, 5))
  val test3 = Mul(4, 7)

  def test() : Unit = {
    println("test0 (1): " + print(test0) + ", " + count(test0))
    println("test1 (4): " + print(test1) + ", " + count(test1))
    println("test2 (2): " + print(test2) + ", " + count(test2))
    println("test3 (2): " + print(test3) + ", " + count(test3))

  }

/**
We can of course also apply other algorithms using visitors, such as counting the number of "Num" literals, or printing to a string:

  val countVisitor = Visitor[Int]( _=>1, _+_)
  val printVisitor = Visitor[String](_.toString, "("+_+"+"+_+")")

}
*/

}
