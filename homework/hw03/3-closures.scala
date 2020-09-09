import scala.language.implicitConversions
/**
Higher-Order Functions

F1-WAE, the language with first-order functions, lets us abstract over patterns that involve numbers. But what if we want to abstract
over patterns that involve functions, such as the "list fold" pattern, whose instantiations include summing or multiplying a list of
integers?

To enable this kind of abstraction, we need to make functions "first-class", which means that they become values that can be passed to
or returned from functions or stored in data structures. Languages with first-class functions enable so-called "higher-order functions",
which are functions that accept or return a (possibly again higher-order) function.

We will see that this extension will make our language both simpler and much more powerful. This seeming contradiction is famously
addressed by the first sentence of the Scheme language specification:

"Programming languages should be designed not by piling feature on top of feature, but by removing the weaknesses and restrictions that
make additional features appear necessary."

The simplicity is due to the fact that this language is so expressive that many other language features can be "encoded", i.e., they do
not need to be added to the language but can be expressed with the existing features.

This language, which we call "FAE", is basically the so-called "lambda calculus", a minimal but powerful programming language that has
been highly influential in the design and theory of programming languages.

FAE is the language of arithmetic expressions, AE, plus only two additional language constructs: Function abstraction and function
application.
 */

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(name: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
implicit def num2exp(n: Int): Num = Num(n)
implicit def id2exp(s: String): Id = Id(s)

/**
Both function definitions and applications are expressions.
 */
case class Fun(param: String, body: Exp) extends Exp
case class App (funExpr: Exp, argExpr: Exp) extends Exp



def freeVars(e: Exp) : Set[String] =  e match {
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
  case Fun(x,body) => freeVars(body) - x
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Num(n) => Set.empty
}
assert(freeVars(Fun("x",Add("x","y"))) == Set("y"))



val test = App( Fun("x",Add("x",5)), 7)


/**
However, consider the following example.
 */
// val test2 = wth("x", 5, App(Fun("f", App("f",3)), Fun("y",Add("x","y"))))
val test2 = App( Fun("x", App( Fun("f", App("f",3)), Fun("y",Add("x","y")) ) ) , 5)

// App( Fun("y",Add("x","y")) , 3)

/**
Evaluation (no closures):

  test1 = (λx.(x+5) 7)
  ~~> With x=7: (x+5)
  ~~> 12

  test2 = (λx.(λf.(f 3) λy.(x+y)) 5)
  ~~> With x=5: (λf.(f 3) λy.(x+y))
  ~~> With f=λy.(x+y): (f 3)
  ~~> (λy.(x+y) 3)
  ~~> With y=3: (x+y)
  ~~> Error: x not bound

  test3 = (λx.(λy.(x+y) 4) 6)
  ~~> With x=6: (λy.(x+y) 4)
  ~~> With y=4: (x+y)
  ~~> Error: x not bound
 */


sealed abstract class Value
type Env = Map[String, Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

/**
The evaluator becomes :
 */

def evalWithEnv(e: Exp, env: Env) : Value = e match {
  case Num(n: Int) => NumV(n)
  case Id(x) => env(x)
  case Add(l,r) =>
    (evalWithEnv(l,env), evalWithEnv(r,env)) match {
      case (NumV(v1),NumV(v2)) => NumV(v1+v2)
      case _ => sys.error("can only add numbers")
    }
  case f@Fun(_, body) => ClosureV(f, env.filter(k => freeVars(body).contains(k._1)))
  case App(f,a) => evalWithEnv(f,env) match {
    // Use environment stored in closure to realize proper lexical scoping!
    case ClosureV(f,closureEnv) => evalWithEnv(f.body, closureEnv + (f.param -> evalWithEnv(a,env)))
    case _ => sys.error("can only apply functions")
  }
}

def evalWithEnvPrint(e: Exp, env: Env) : Value = e match {
  case Num(n: Int) => NumV(n)
  case Id(x) => env(x)
  case Add(l,r) =>
    (evalWithEnvPrint(l,env), evalWithEnvPrint(r,env)) match {
      case (NumV(v1),NumV(v2)) => NumV(v1+v2)
      case _ => sys.error("can only add numbers")
    }
  case f@Fun(_, body) =>
    print("Filtered env: " + env.filter(pair => freeVars(body).contains(pair._1)) + "\n")
    ClosureV(f, env.filter(pair => freeVars(body).contains(pair._1)))
  case App(f,a) => evalWithEnvPrint(f,env) match {
    // Use environment stored in closure to realize proper lexical scoping!
    case ClosureV(f,closureEnv) =>
      val newEnv = closureEnv + (f.param -> evalWithEnvPrint(a,env))
      println("body: " + f.body)
      println("closureEnv: " + newEnv)
      println("\n")
      evalWithEnvPrint(f.body, newEnv)
    case _ => sys.error("can only apply functions")
  }
}

val test3 = App(Fun("x", App(Fun("y", Add("x","y")), 4)), 6)
assert( evalWithEnv(test, Map.empty) == NumV(12))
assert( evalWithEnv(test2,Map.empty) == NumV(8))
assert( evalWithEnv(test3,Map.empty) == NumV(10))
val test4 = App(Fun("f", App(Fun("a", App(Fun("b", App("f", Add("a", "b"))), 3)), 7)), Fun("x", Add("x",2)))
assert(evalWithEnv(test4, Map.empty) == NumV(12))
val test5 = App(Fun("a", App(Fun("b", App(Fun("x", Add("x",2)), Add(4, 5))), 3)), 7)
assert(evalWithEnv(test5, Map.empty) == NumV(11))

// evaluates to closure containing λy.(x+y) and environment {x -> 5}
val test6 = App(Fun("x", Fun("y", Add("x","y"))), 5)
assert(evalWithEnv(test6, Map.empty) == ClosureV(Fun("y", Add("x","y")), Map("x" -> NumV(5))))

/**
Some examples:

  test = (λx.(x+5) 7)
  test2 = (λx.(λf.(f 3) λy.(x+y)) 5)
  test3 = (λx.(λy.(x+y) 4) 6)
  test4 = (λf.(λa.(λb.(f (a+b)) 3) 7) λx.(x+2))

Evaluation with closures:

  test1 = (λx.(x+5) 7), env = {}
  ~~> (x+5), env = {} ++ {x->5}
  ~~> 12

  test2 = (λx.(λf.(f 3) λy.(x+y)) 5), env = {}
  ~~> (λf.(f 3) λy.(x+y)), env = {} ++ {x->5}
  ~~> (f 3), env = {x->5} ++ { f->[λy.(x+y),{x->5}] }
  ~~> (λy.(x+y) 3), env = { x->5, f->[λy.(x+y),{x->5}] }
  ~~> (x+y), env = { x->5, f->[λy.(x+y),{x->5}] } ++ {y->3}


  test3 = (λx.(λy.(x+y) 4) 6), env = {}
  ~~> (λy.(x+y) 4), env = {} ++ {x->6}
  ~~> With y=4: (x+y), env = {x->6} ++ {y->4}
  ~~> (4+6) ~~> 10

  test4 = (λf.(λa.(λb.(f (a+b)) 3) 7) λx.(x+2)), {}
  ~~> (λa.(λb.(f (a+b)) 3) 7), { f -> [λx.(x+2),{}] }
  ~~> (λb.(f (a+b)) 3), { a -> 7, f -> [λx.(x+2),{}] }
  ~~> (f (a+b)), { f -> [λx.(x+2),{}], a -> 7, b -> 3 }
  ~~> (x+2),  { f -> [λx.(x+2),{}], a -> 7, b -> 3, x -> 10 }
  ~~>* NumV(12)
 */

/**
 * Converts expression to String in Lambda calculus notation for better legibility. When printing to console the lambda
 * symbol may not be displayed correctly.
 * @param e Expression to be printed
 * @return Lambda calculus representation of e
 */
def printExp(e: Exp) : String = e match {
  case Num(n: Int) => n.toString
  case Id(x) => x
  case Add(l,r) => "(" + printExp(l) + "+" + printExp(r) + ")"
  case Fun(param, body) => "λ" + param + "." + printExp(body)
  case App(f,a) => "(" + printExp(f) + " " + printExp(a) + ")"
}