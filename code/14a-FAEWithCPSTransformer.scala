import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)

/** CPS expression data type */
sealed abstract class CPSExp
sealed abstract class CPSVal extends CPSExp
case class CPSNum(n: Int) extends CPSVal
case class CPSVar(x: String) extends CPSVal { override def toString: String = x }
case class CPSFun(x: String, k: String, body: CPSExp) extends CPSVal
case class CPSCont(v: String, body: CPSExp) extends CPSVal
case class CPSAdd(l: CPSVar, r: CPSVar) extends CPSVal

case class CPSFunApp(f: CPSVar, a: CPSVar, k: CPSVar) extends CPSExp
case class CPSContApp(k: CPSVal, a: CPSVal) extends CPSExp

implicit def string2cpsExp(s: String): CPSVar = CPSVar(s)

def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set()
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l)++freeVars(r)
  case Fun(p,b) => freeVars(b)-p
  case App(f,a) => freeVars(f)++freeVars(a)
}

def freshName(names: Set[String], default: String) : String = {
  var last: Int = 0
  var freshName = default
  while (names(freshName)) {
    freshName = default+last.toString
    last += 1
  }
  freshName
}

def cps(e: Exp) : CPSCont = e match {
  case Num(n) => CPSCont("k", CPSContApp("k", CPSNum(n)))
  case Id(x) =>
    val k = freshName(freeVars(e),"k")
    CPSCont(k, CPSContApp(k,CPSVar(x)))
  case Add(l,r) =>
    val k = freshName(freeVars(e),"k")
    val lv = freshName(freeVars(r),"lv")
    CPSCont(k, CPSContApp(cps(l), // no fresh name for rv, since the name
      CPSCont(lv, CPSContApp(cps(r), // cannot appear in following expression
        CPSCont("rv", CPSContApp(k, CPSAdd(lv,"rv")))))))
  case Fun(p,b) =>
    val k = freshName(freeVars(e),"k")
    val dynK = freshName(freeVars(e),"dynK")
    CPSCont(k, CPSContApp(k,
      CPSFun(p, dynK, CPSContApp(cps(b),dynK))))
  case App(f,a) =>
    val k = freshName(freeVars(e), "k")
    val fVal = freshName(freeVars(e), "fVal")
    CPSCont(k, CPSContApp(cps(f),
      CPSCont(fVal, CPSContApp(cps(a),
        CPSCont("aVal", CPSFunApp(fVal,"aVal",k))))))
}



