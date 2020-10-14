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

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names contains freshName) {
    freshName = default + last
    last += 1
  }
  freshName
}

def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set.empty
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
  case Mul(l,r) => freeVars(l) ++ freeVars(r)
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Fun(x,body) => freeVars(body) - x
}

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


def evalCBN(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (evalCBN(l),evalCBN(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (evalCBN(l),evalCBN(r)) match {
    case (Num(a), Num(b)) => Num(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => evalCBN(f) match {
    // case Fun(param,body) => eval(subst(body,param,eval(a))) // call-by-value
    case Fun(param,body) => evalCBN(subst(body,param,a)) // call-by-name
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}

// Returns Fun(y, Add(2,y)) under CBV evaluation and Fun(y, Add(Add(1,1),y)) under CBN evaluation
val ex = App(Fun("x", Fun("y", Add("x","y"))), Add(1,1))

