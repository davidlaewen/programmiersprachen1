/**
Note: For some tasks, test examples are already provided.
Be sure to provide tests for all tasks and check your solution with them.
From now on, the tasks will not explicitly require tests any more,
but I advise you to nevertheless use tests for all programming tasks.
 */

import scala.language.implicitConversions

/**
Part 2: Binding constructs (2 sub-tasks, plus 1 optional subtask)
------
Consider the language of arithmetic expressions with "with",
as illustrated by the following abstract syntax:
 */

object Hw02b {

  sealed abstract class Exp
  case class Num(n: Int) extends Exp
  case class Add(lhs: Exp, rhs: Exp) extends Exp
  case class Mul(lhs: Exp, rhs: Exp) extends Exp
  case class Id(x: String) extends Exp
  case class With(x: String, xdef: Exp, body: Exp) extends Exp

  /**
   * We use implicits again to make example programs less verbose.
   */
  implicit def num2exp(n: Int) = Num(n)
  implicit def str2exp(x: String) = Id(x)

  /**
   * Your task is to extend the language with the following new binding construct:
   */

  case class Let(defs: List[(String, Exp)], body: Exp) extends Exp
  case class LetStar(defs: List[(String, Exp)], body: Exp) extends Exp

  /**
   * The purpose of the Let construct is to bind a list of identifiers in such a way
   * that the scope of the bound variables is only in the body, but not any of the
   * right hand sides of definitions. In particular, there is no shadowing between the definitions.
   * For instance, the following test case should evaluate to 7 and not to 11:
   */

  val test1 =
    With("x", 1,
      Let(List("x" -> 5, "y" -> Add("x", 1)), Add("x", "y")))

  /**
   * Note: The names "Let" and "LetStar" (see below) have been chosen in analogy to the
   * "let" and "let*" binding constructs in Scheme and Racket.
   */

  /**
   * Subtasks:
   *
   * 1) Implement the missing part of the eval and subst function
   * to support Let. Only change the parts currently filled with an error!
   *
   * 2) There is some redundancy in the binding constructs of this
   * language. Eliminate the construct With by defining it as
   * syntactic sugar.
   *
   * 3) Third exercise: See below.
   */

  def desugar(e: Exp): Exp = e match {
    case Num(n) => e
    case Id(x) => e
    case Add(l, r) => Add(desugar(l), desugar(r))
    case Mul(l, r) => Mul(desugar(l), desugar(r))
    case With(x, xdef, body) => Let(List(x -> desugar(xdef)), desugar(body))
    case Let(defs, body) => Let(defs.map{ case (s, e) => (s, desugar(e)) }, desugar(body))
    case LetStar(defs, body) => LetStar(defs.map{ case (s, e) => (s, desugar(e)) }, desugar(body))
  }

  def subst(e: Exp, i: String, v: Num): Exp = e match {
    case Num(n) => e
    case Id(x) => if (x == i) v else e
    case Add(l, r) => Add(subst(l, i, v), subst(r, i, v))
    case Mul(l, r) => Mul(subst(l, i, v), subst(r, i, v))
    case With(x, xdefs, body) => sys.error("'With' was not desugared!")
    case Let(defs, body) => Let(defs.map{ case (s, e) => (s, subst(e, i, v)) },
      if (!defs.exists{ case (s, e) => s == i }) subst(body, i, v)
      else body) // only substitute in body if there is no new def for i
    case LetStar(defs, body) => LetStar(substLetStar(defs, i, v),
      if (!defs.exists{ case (s, e) => s == i }) subst(body, i, v)
      else body) // only substitute in body if there is no new def for i
  }

  def substLetStar(defs: List[(String, Exp)], i: String, v: Num): List[(String, Exp)] = {
    if (defs.isEmpty)
      List()
    else // substitute in head, concatenate with rest of list (after substitution)
      List(defs.head._1 -> subst(defs.head._2,i,v)).concat(
        if (defs.head._1 == i) defs.drop(1) else substLetStar(defs.drop(1),i,v))
  }

  def eval(e: Exp): Int = e match {
    case Num(n) => n
    case Id(x) => sys.error("Unbound variable: " + x)
    case Add(l, r) => eval(l) + eval(r)
    case Mul(l, r) => eval(l) * eval(r)
    case With(x, xdef, body) => sys.error("'With' was not desugared!")
    case Let(defs, body) =>
      if (defs.isEmpty) eval(body)
      else eval(Let(defs.drop(1), subst(body, defs.head._1, Num(eval(defs.head._2)))))
    case LetStar(defs, body) =>
      if (defs.isEmpty) eval(body)
      else eval(subst(LetStar(defs.drop(1), body), defs.head._1, Num(eval(defs.head._2))))
  }

  /**
   * Third exercise (3)
   * The LetStar construct is similar to let, but the scope of a definition contains all
   * right hand sides of definitions that follow the current one.
   * The following test case should hence evaluate to 11.
   */

  val test2 =
    With("x", 1,
      LetStar(List("x" -> 5, "y" -> Add("x", 1)), Add("x", "y")))

  /**
   * Your task: Implement the missing parts of subst and eval to support LetStar.
   * (Again, only change the parts currently filled with an error!)
   * Bonus task (not mandatory): Eliminate LetStar by defining it as syntactic sugar.
   */

  // additional test cases
  val test3 =
  With("a", 2,
    Let(List("b" -> Mul("a", 20)), Add("a", "b")))

  val test4 =
    Let(List("a" -> 5), Let(List("a" -> 7), Let(List("a" -> 21), Mul("a", 2))))

  val test5 =
    With("x", 13,
      LetStar(List("x" -> 20, "y" -> Add("x", 1)), Mul("y", 2)))

  val test6 =
    LetStar(List("x" -> 2, "y" -> Mul("x", "x"), "z" -> Add("x", "y")), Add(10, "z"))

  val test7 =
    Let(List("x" -> 25), With("x", "x",
      LetStar(List("x" -> "x", "x" -> "x", "x" -> "x"), "x")))


  // eval() call on test cases
  def evalTest(): Unit = {
    println("test1  (7): " + eval(desugar(test1)))
    println("test2 (11): " + eval(desugar(test2)))
    println("test3 (42): " + eval(desugar(test3)))
    println("test4 (42): " + eval(desugar(test4)))
    println("test5 (42): " + eval(desugar(test5)))
    println("test6 (16): " + eval(desugar(test6)))
    println("test7 (25): " + eval(desugar(test7)))
  }

}