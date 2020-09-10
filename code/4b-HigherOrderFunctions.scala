import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

type Env = Map[String, Int]

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

// define With as syntactic sugar:
def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x,body), xdef)


// generate new name which is not included in 'names'
def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names contains freshName) {
    freshName = default + last
    last += 1
  }
  freshName
}

// returns Set of all free variables in an expression
def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set.empty
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
  case Mul(l,r) => freeVars(l) ++ freeVars(r)
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Fun(x,body) => freeVars(body) - x
}

assert(freeVars(Fun("x", Add("x","y"))) == Set("y"))


def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else e
  case Fun(param,body) =>
    if (param == i) e else {
      val fvs = freeVars(e) ++ freeVars(v) + i
      val newVar = freshName(fvs,param)
      Fun(newVar, subst(subst(body,param,Id(newVar)), i, v))
    }
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}


def eval(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l),eval(r)) match {
    case (Num(a), Num(b)) => Num(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(param,body) => eval(subst(body,param,eval(a))) // call-by-value
    // case Fun(param,body) => eval(subst(body,param,a)) // call-by-name
    case _ => sys.error("Can only apply functions")
  }
  case _ => e // numbers and functions evaluate to themselves
}


/**
def evalWithEnv(env: Env, e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => evalWithEnv(env,l) + evalWithEnv(env,r)
  case Mul(l,r) => evalWithEnv(env,l) * evalWithEnv(env,r)
  case Id(x) => env(x)
  case Fun(param,body) =>
  case App(f,a) => f match {
    case Fun(param, body) =>
      evalWithEnv(env+(param -> evalWithEnv(env,a)),body)
    case _ => sys.error("Can only apply functions!")
  }
}
*/

val a = wth("b", Num(2), App(Fun("a", Add("a",1)), Add(3,"b")))
assert(eval(a) == Num(6))
val b = Fun("x","x")
assert(eval(b) == Fun("x","x"))
val c = App(Fun("x", App(Fun("x",Mul(2,"x")), Add(1,"x"))), 1)
assert(eval(c) == Num(4))
val d = App(Fun("f", App("f", App("f", 2))), Fun("x",Mul(2,"x"))) // apply double 2 times
assert(eval(d) == Num(8))
val omega = App( Fun("x", App("x","x")), Fun("x", App("x","x")))

val test = (subst(App("x","x"), "x", Fun("x", App("x","x"))) ==
  App(Fun("x", App("x","x")), Fun("x", App("x","x"))))