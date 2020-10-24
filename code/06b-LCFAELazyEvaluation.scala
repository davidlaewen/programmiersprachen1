import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(l: Exp, r: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp


/** Lazy evaluation alternatives */
trait CBN {
  type Thunk // abstract type member

  case class Env(map: Map[String, Thunk]) {
    def apply(key: String): Thunk = map.apply(key)
    def +(other: (String, Thunk)) : Env = Env(map+other)
  }

  def delay(e: Exp, env: Env) : Thunk
  def force(t: Thunk) : Value

  sealed abstract class Value
  case class NumV(n: Int) extends Value
  case class ClosureV(f: Fun, env: Env) extends Value
  def eval(e: Exp, env: Env) : Value = e match {
    case Id(x) => force(env(x)) // force evaluation of thunk
    case Add(l,r) =>
      (eval(l,env), eval(r,env)) match {
        case (NumV(v1),NumV(v2)) => NumV(v1+v2)
        case _ => sys.error("can only add numbers")
      }
    case App(f,a) => eval(f,env) match {
      // delay argument and add it to environment of the closure
      case ClosureV(f,cEnv) => eval(f.body, cEnv + (f.param -> delay(a,env)))
      case _ => sys.error("can only apply functions")
    }
    case Num(n) => NumV(n)
    case f@Fun(_,_) => ClosureV(f,env)
  }
}

object CallByName extends CBN {
  case class Thunk(exp: Exp, env: Env)
  def delay(e: Exp, env: Env): Thunk = Thunk(e,env)
  def force(t: Thunk): CallByName.Value = {
    println("Forcing evaluation of expression "+t.exp)
    eval(t.exp,t.env)
  }
}

object CallByNeed extends CBN {
  case class MemoThunk(exp: Exp, env: Env) {
    var cache: Value = _
  }
  type Thunk = MemoThunk
  def delay(e: Exp, env: Env): MemoThunk = MemoThunk(e,env)
  def force(t: Thunk): CallByNeed.Value = {
    if (t.cache != null)
      println ("Reusing cached value "+t.cache+" for expression "+t.exp)
    else {
      println("Forcing evaluation of expression: "+t.exp)
      t.cache = eval(t.exp, t.env)
    }
    t.cache
  }
}

val ex1 = App(Fun("x",Add(Add("x","x"),Add("x","x"))), Add(2,2))
// CallByName.eval(ex1, CallByName.Env(Map()))
// CallByNeed.eval(ex1, CallByNeed.Env(Map()))






















