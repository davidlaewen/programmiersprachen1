/**
Consider the following language of propositional logic formulae:
 */
sealed abstract class Exp
case class True() extends Exp  // constant true
case class False() extends Exp // constant false
case class And(lhs: Exp, rhs: Exp) extends Exp
case class Or(lhs: Exp, rhs: Exp) extends Exp
case class Not(e: Exp) extends Exp
case class Impl(p: Exp, q: Exp) extends Exp

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
  case True()     => true
  case False()    => false
  case And(l, r)  => eval(l) && eval(r)
  case Or(l, r)   => eval(l) || eval(r)
  case Not(e)     => !eval(e)
  case Impl(p, q) => implies(eval(p), eval(q))
}


// Helper function for logical implication
def implies(p: Boolean, q: Boolean) : Boolean = {
  if (p == q)
    return true
  else
    return q
}

val exampleProposition = And(Not(True()), False()) // should evaluate to false
val ex1 = Or(True(), True())
val ex2 = Not(False())
val ex3 = Or(False(), Not(True()))
val ex4 = Impl(True(), True())
val ex5 = Impl(False(), True())
val ex6 = Impl(True(), False())

def runTest() : Unit = {
  println("All values should be true\n")
  println("ex0: " + !eval(exampleProposition))
  println("ex1: " + eval(ex1))
  println("ex2: " + eval(ex2))
  println("ex3: " + !eval(ex3))
  println("ex4: " + eval(ex4))
  println("ex5: " + eval(ex5))
  println("ex6: " + !eval(ex6))
}