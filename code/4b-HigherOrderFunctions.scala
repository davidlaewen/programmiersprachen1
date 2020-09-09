import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

// define With as syntactic sugar:
def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x,body), xdef)

type Env = Map[String, Int]

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

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


def evalWithSubst(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => evalWithSubst(l) + evalWithSubst(r)
  case Mul(l,r) => evalWithSubst(l) * evalWithSubst(r)
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Fun(param,body) => sys.error("Result is a function")
  case App(f,a) => f match {
    case Fun(param,body) =>
      evalWithSubst(subst(body,param,evalWithSubst(a)))
    case _ => sys.error("Can only apply functions!")
  }
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
assert(evalWithSubst(a) == 6)

// result: Fun(x, Add(x, Add(x, 5))) - x is accidentally captured!
