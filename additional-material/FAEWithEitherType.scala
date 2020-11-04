import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(l: Exp, r: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

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
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Fun(x,body) => freeVars(body) - x
}

def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else e
  case Fun(p,b) =>
    if (p == i) e else {
      val fvs = freeVars(e) ++ freeVars(v) + i
      val nn = freshName(fvs,p)
      Fun(nn, subst(subst(b,p,Id(nn)), i, v))
    }
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}

def eval(e: Exp) : Either[Num,Fun] = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Left(Num(a)),Left(Num(b))) => Left(Num(a+b))
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Right(Fun(param,body)) => eval(a) match {
      case Left(av) => eval(subst(body,param,av))
      case Right(av) => eval(subst(body,param,av))
    }
    case _ => sys.error("Can only apply functions")
  }
  case n@Num(_) => Left(n)
  case f@Fun(_,_) => Right(f)
}

