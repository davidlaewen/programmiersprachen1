/**
Note: For some tasks, test examples are already provided.
Be sure to provide tests for all tasks and check your solution with them.
From now on, the tasks will not explicitly require tests any more,
but I advise you to nevertheless use tests for all programming tasks.
 */

/**
Part 2: Binding constructs (2 sub-tasks, plus 1 optional subtask)
------
Consider the language of arithmetic expressions with "with",
as illustrated by the following abstract syntax:
 */

object Hw02c {

  import scala.language.implicitConversions

  sealed abstract class Exp
  case class Num(n: Int) extends Exp
  case class Add(lhs: Exp, rhs: Exp) extends Exp
  case class Mul(lhs: Exp, rhs: Exp) extends Exp
  case class Id(x: String) extends Exp

  /**
   * We use implicits again to make example programs less verbose.
   */
  implicit def num2exp(n: Int) = Num(n)
  implicit def str2exp(x: String) = Id(x)

  /**
   * Your task is to extend the language with the following new binding construct:
   */

  case class Let(defs: List[(String, Exp)], body: Exp) extends Exp
  
  /**
   * The purpose of the Let construct is to bind a list of identifiers in such a way
   * that the scope of the bound variables is only in the body, but not any of the
   * right hand sides of definitions. In particular, there is no shadowing between the definitions.
   * For instance, the following test case should evaluate to 7 and not to 11:
   */

  val test1 =
    wth("x", 1,
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
   
  // 'With' as syntactic sugar
  def wth(x: String, xDef: Exp, body: Exp) : Exp = Let(List(x->xDef),body)


  def subst(e: Exp, i: String, v: Num): Exp = e match {
    case Num(n) => e
    case Id(x) => if (x == i) v else e
    case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
    case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
    case Let(defs, body) => Let(defs.map{ case (s,e) => (s, subst(e,i,v)) },
      if (defs.map(ie => ie._1).contains(i)) body else subst(body, i, v)) 
      // only substitute in body if there is no new def for i
  }

  def substDefs(defs: List[(String, Exp)], i: String, v: Num): List[(String, Exp)] = defs match {
    case List() => List()
    case (i1,v1)::rest => 
      (i1,subst(v1,i,v))::(if (i1==i) rest else substDefs(rest,i,v))
    // stop substituting if new binding for i occurs in list
  }

  def eval(e: Exp): Int = e match {
    case Num(n) => n
    case Id(x) => sys.error("Unbound variable: " + x)
    case Add(l, r) => eval(l) + eval(r)
    case Mul(l, r) => eval(l) * eval(r)
    case Let(defs, body) => 
      eval( defs.foldLeft(body)( (b,ie) => subst(b,ie._1,Num(eval(ie._2)))) )
  }

  /**
   * Third exercise (3)
   * The LetStar construct is similar to let, but the scope of a definition contains all
   * right hand sides of definitions that follow the current one.
   */
   
  // 'LetStar' as syntactic sugar
  def letStar(defs: List[(String,Exp)], body: Exp) : Exp = defs match {
    case List() => body
    case ie::rest => Let(List(ie), letStar(rest,body))
  }
   
  /**
   * The following test case should hence evaluate to 11.
   */

  val test2 =
    wth("x", 1,
      letStar(List("x" -> 5, "y" -> Add("x", 1)), Add("x", "y")))

  /**
   * Your task: Implement the missing parts of subst and eval to support LetStar.
   * (Again, only change the parts currently filled with an error!)
   * Bonus task (not mandatory): Eliminate LetStar by defining it as syntactic sugar.
   */

  // additional test cases
  val test3 =
  wth("a", 2,
    Let(List("b" -> Mul("a", 20)), Add("a", "b")))

  val test4 =
    Let(List("a" -> 5), Let(List("a" -> 7), Let(List("a" -> 21), Mul("a", 2))))

  val test5 =
    wth("x", 13,
      letStar(List("x" -> 20, "y" -> Add("x", 1)), Mul("y", 2)))

  val test6 =
    letStar(List("x" -> 2, "y" -> Mul("x", "x"), "z" -> Add("x", "y")), Add(10, "z"))

  val test7 =
    Let(List("x" -> 25), wth("x", "x",
      letStar(List("x" -> "x", "x" -> "x", "x" -> "x"), "x")))


  // eval() call on test cases
  def evalTest(): Unit = {
    println("test1  (7): " + eval(test1))
    println("test2 (11): " + eval(test2))
    println("test3 (42): " + eval(test3))
    println("test4 (42): " + eval(test4))
    println("test5 (42): " + eval(test5))
    println("test6 (16): " + eval(test6))
    println("test7 (25): " + eval(test7))
  }

}
