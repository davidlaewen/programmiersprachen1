/**
Note: For some tasks, test examples are already provided.
Be sure to provide tests for all tasks and check your solution with them.
From now on, the tasks will not explicitly require tests any more,
but I advise you to nevertheless use tests for all programming tasks.
 */

import scala.language.implicitConversions

object Hw02a {
  /**
  Consider again the language of propositional logic formulae from the previous homework:
   */
  sealed abstract class Exp
  case class True() extends Exp  // constant true
  case class False() extends Exp // constant false
  case class And(lhs: Exp, rhs: Exp) extends Exp
  case class Or(lhs: Exp, rhs: Exp) extends Exp
  case class Not(e: Exp) extends Exp
  case class Impl(lhs: Exp, rhs: Exp) extends Exp
  case class Nand(lhs: Exp, rhs: Exp) extends Exp

  def eval(e: Exp) : Boolean = e match {
    case True()         => true
    case False()        => false
    case Nand(lhs, rhs) => !(eval(lhs) && eval(rhs))
    case _ => sys.error("Desugaring error!")
  }

  // converts all occurences of And(), Or(), Not(), Impl() to Expressions containing
  // only True(), False() and Nand()
  def desugar(e: Exp) :  Exp = e match {
    case And(lhs: Exp, rhs: Exp)  => desugar(Not(Nand(lhs,rhs)))
    // lhs and rhs == not(lhs nand rhs)
    case Or(lhs: Exp, rhs: Exp)   => Nand(desugar(Not(lhs)), desugar(Not(rhs)))
    // lhs or rhs == (not lhs) nand (not rhs)
    case Not(e: Exp)              => Nand(desugar(e), desugar(e))
    // not e == e nand e
    case Impl(lhs: Exp, rhs: Exp) => Nand(desugar(lhs), desugar(Not(rhs)))
    // lhs => rhs == lhs nand (not rhs)
    case _                        => e
    // else apply no desugaring
  }

  // example values for testing
  val ex1 = And(True(), True())
  val ex2 = Not(And(True(), False()))
  val ex3 = Or(True(), False())
  val ex4 = Not(False())
  val ex5 = Impl(ex1, ex2)
  val ex6 = Impl(Not(ex3), Not(ex4))
  val ex7 = Impl(Not(ex5), ex6)

  def evalTest() : Unit = {
    println("All values should be true:")
    println(eval(desugar(ex1)))
    println(eval(desugar(ex2)))
    println(eval(desugar(ex3)))
    println(eval(desugar(ex4)))
    println(eval(desugar(ex5)))
    println(eval(desugar(ex6)))
    println(eval(desugar(ex7)))
  }

  /**
  Subtasks:
      
      1) Introduce a new kind of expression "Nand" (not both ... and ...).
      Eliminate And, Or, Not, and Impl by defining them as syntactic sugar for Nand.
   */

}