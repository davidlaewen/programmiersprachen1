object Hw01 {

  /**
  Consider the following language of propositional logic formulae:
   */
  sealed abstract class Exp
  case class True() extends Exp  // constant true
  case class False() extends Exp // constant false
  case class And(lhs: Exp, rhs: Exp) extends Exp
  case class Or(lhs: Exp, rhs: Exp) extends Exp
  case class Not(e: Exp) extends Exp
  case class Impl(lhs: Exp, rhs: Exp) extends Exp

  /**
  Tasks:

       1) Implement the missing parts of the interpreter for these formulae
          (the eval function).
          Test the correctness by evaluating the example proposition given
          below and add at least two more examples and test against these.
​
       2) Add implication as a new kind of expression "Impl" and extend
          the interpreter accordingly. Add at least two examples and test.
   */

  def eval(e: Exp) : Boolean = e match {
    case True()    => true
    case False()   => false
    case And(l, r) => eval(l) && eval(r)
    case Or(l, r)  => eval(l) || eval(r)
    case Not(e)    => !eval(e)
    case Impl(l,r) => !eval(l) || eval(r)
  }

  val a = And(Not(True()), False()) // false
  val b = Or(And(False(), True()), Not(False())) // true

  val c = Impl(False(), False()) // true
  val d = Impl(False(), True()) // true
  val e = Impl(True(), False()) // false
  val f = Impl(True(), True()) // true

  def test() : Unit = {
    println("a = " + eval(a))
    println("b = " + eval(b))
    println("c = " + eval(c))
    println("d = " + eval(d))
    println("e = " + eval(e))
    println("f = " + eval(f))
  }


}
