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

def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else Id(x)
  case Fun(param,body) =>
    if (param == i) e else Fun(param, subst(body,i,v))
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}

def evalWithSubst(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => evalWithSubst(l) + evalWithSubst(r)
  case Mul(l,r) => evalWithSubst(l) * evalWithSubst(r)
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Fun(param,body) => ???
  case App(f,a) => f match {
    case Fun(param,body) =>
      evalWithSubst(subst(body,param,evalWithSubst(a)))
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

