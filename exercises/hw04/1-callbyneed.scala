import scala.language.implicitConversions

object Hw04 {

  sealed abstract class Exp
  case class Num(n: Int) extends Exp
  case class Id(name: String) extends Exp
  case class Add(lhs: Exp, rhs: Exp) extends Exp
  implicit def num2exp(n: Int) = Num(n)
  implicit def id2exp(s: String) = Id(s)

  case class Fun(param: String, body: Exp) extends Exp
  // Added for the task
  case class LazyFun(param: String, body: Exp) extends Exp
  case class App (funExpr: Exp, argExpr: Exp) extends Exp

  case class Bool(b: Boolean) extends Exp
  implicit def bool2exp(b: Boolean) = Bool(b)
  case class If(test: Exp, thenB: Exp, elseB: Exp) extends Exp
  case class Eq(lhs: Exp, rhs: Exp) extends Exp
  def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x,body),xdef)
  //syntactic sugar
  def not(e: Exp): Exp = If(e, false, true)
  def or(l: Exp, r: Exp) = If(l, true, r)
  def and(l: Exp, r: Exp) =
    If(l, r, false)

  val test1 = App( Fun("x",Add("x",5)), 7)
  val test2 = wth("x", 5, App(Fun("f", App("f",3)), Fun("y",Add("x","y"))))

  def freeVars(e: Exp): Set[String] = e match {
    case Id(x)            => Set(x)
    case Add(l, r)        => freeVars(l) ++ freeVars(r)
    case Fun(x, body)     => freeVars(body) - x
    // Extended for the task
    case LazyFun(x, body) => freeVars(body) - x
    case App(f, a)        => freeVars(f) ++ freeVars(a)
    case Num(n)           => Set.empty
    case Bool(b)          => Set.empty
    case If(test, t, e)   => freeVars(test) ++ freeVars(t) ++ freeVars(e)
    case Eq(l, r)         => freeVars(l) ++ freeVars(r)
  }

  sealed abstract class Value
  case class NumV(n: Int) extends Value
  case class BoolV(b: Boolean) extends Value
  // Extended for the task: closes either over a Fun or a LazyFun
  // Note: Either is from the Scala standard lib.
  // It is a disjoint union type into which one can inject with the
  // constructors Left and Right.
  case class ClosureV(f: Either[Fun, LazyFun], env: Env) extends Value

  // Extended for the task: A symbol can map either to a thunk or to a value
  type Env = Map[String, EnvValue]
  sealed abstract class EnvValue
  case class EnvNonThunk(t: Value) extends EnvValue
  case class EnvThunk(t: Thunk) extends EnvValue

  // Extended for the task: Usual implementation of call-by-need thunks
  case class Thunk(e: Exp, env: Env) {
    var cache: Value = null
  }

  def delay(e: Exp, env: Env): Thunk = Thunk(e, env)

  private def forceEval(e: Exp, env: Env): Value = {
    println("Forcing evaluation of expression: " + e)
    evalWithEnv(e, env)
  }
  def force(t: Thunk): Value = {
    if (t.cache == null)
      t.cache = forceEval(t.e, t.env)
    else
      println("Reusing cached value " + t.cache + " for expression " + t.e)
    t.cache
  }

  def evalWithEnv(e: Exp, env: Env) : Value = e match {
    case Num(n) => NumV(n)
    case Bool(b) => BoolV(b)
    case Id(x) =>
      // Extended for the task: If the symbol maps to a thunk in the environment, force the thunk
      env(x) match {
        case EnvThunk(t)     => force(t)
        case EnvNonThunk(nt) => nt
      }
    case Add(l,r) => {
      (evalWithEnv(l,env), evalWithEnv(r,env)) match {
        case (NumV(v1),NumV(v2)) => NumV(v1+v2)
        case _ => sys.error("can only add numbers")
      }
    }
    case f@Fun(param,body) =>
      ClosureV(Left(f), env)
    case f@LazyFun(param,body) =>
      ClosureV(Right(f), env)
    case App(f,a) => evalWithEnv(f,env) match {
      // Use environment stored in closure to realize proper lexical scoping!
      case ClosureV(f,closureEnv) =>
        // Extended for the task:
        // - When the closure is for a Fun, add a mapping from the function parameter to the evaluated argument.
        val (funParam, funBody, argValue) = f match {
          case Left(Fun(param, body)) =>
            (param, body, EnvNonThunk(evalWithEnv(a, env)))
          // - When the closure is for a LazyFun, add a mapping to the thunk for the argument.
          case Right(LazyFun(param, body)) =>
            (param, body, EnvThunk(Thunk(a, env)))
        }
        evalWithEnv(funBody, closureEnv + (funParam -> argValue))
      case _ => sys.error("can only apply functions")
    }
    case If(test, t, e) =>
      evalWithEnv(test, env) match {
        case BoolV(b) =>
          if (b)
            evalWithEnv(t, env)
          else
            evalWithEnv(e, env)
        case _ =>
          sys.error("can only branch on booleans")
      }
    case Eq(l, r) =>
      (evalWithEnv(l, env), evalWithEnv(r, env)) match {
        case (NumV(v1), NumV(v2)) => BoolV(v1 == v2)
        case (BoolV(x), BoolV(y)) => BoolV(x == y)
        case _                    => sys.error("can only compare numbers or booleans")
      }
  }

  assert(evalWithEnv(test1, Map.empty) == NumV(12))
  assert(evalWithEnv(test2, Map.empty) == NumV(8))

  // LazyFun test cases
  val test3 = App( LazyFun("x",Add("x",5)), 7)
  val test4 = wth("x", 5, App(LazyFun("f", App("f",3)), LazyFun("y",Add("x","y"))))

  val omega = App( LazyFun("x", App("x", "x")), LazyFun("x", App("x", "x"))) // (λx.(x x) λx.(x x))

  val test5 = App( Fun("x", Add(2, 3)), omega ) // (λx.(2+3) omega)
  val test6 = App( LazyFun("x", Add(2, 3)), omega )

  def evalTest() : Unit = {
    println( "test3 (12): " + evalWithEnv(test3, Map.empty) )
    println( "test4  (8): " + evalWithEnv(test4, Map.empty) )
    // println( "test5  (5): " + evalWithEnv(test5, Map.empty) )
    println( "test6  (5): " + evalWithEnv(test6, Map.empty) )
  }

  val staticScopingTest = wth("y", 5, App(Fun("f", wth("y", 3, App("f", 2))), Fun("z", Add("y", "z"))))
  assert(evalWithEnv(staticScopingTest, Map.empty) == NumV(7))


  def ifWrapper(x: Exp, thenB: Exp, elseB: Exp) : Exp = {
    If(App(LazyFun("", x), 0), App(LazyFun("", thenB), 0), App(LazyFun("", elseB), 0))
  }

  def BadIfWrapper(x: Exp, thenB: Exp, elseB: Exp) : Exp = {
    If(App(Fun("", x), 0), App(Fun("", thenB), 0), App(Fun("", elseB), 0))
  }

  // ¯\_(ツ)_/¯

}