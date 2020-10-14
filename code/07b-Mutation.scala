import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
case class If0(cond: Exp, thenExpr: Exp, elseExpr: Exp) extends Exp
case class NewBox(e: Exp) extends Exp
case class SetBox(b: Exp, e: Exp) extends Exp
case class OpenBox(b: Exp) extends Exp
case class Seq(e1: Exp, e2: Exp) extends Exp

sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

type Address = Int
case class AddressV(a: Address) extends Value
type Store = Map[Address,Value]


implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


var address = 0
def nextAddress : Address = {
  address += 1
  address
}

def eval(e: Exp, env: Env, s: Store) : (Value,Store) = e match {
  case Num(n) => (NumV(n),s)
  case Id(x) => (env(x),s)
  case f@Fun(_,_) => (ClosureV(f,env),s)
  case Add(l,r) => eval(l,env,s) match {
    case (NumV(a),s1) => eval(r,env,s1) match {
      case (NumV(b),s2) => (NumV(a+b),s2)
      case _ => sys.error("Can only add numbers")
    }
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => eval(l,env,s) match {
    case (NumV(a),s1) => eval(r,env,s1) match {
      case (NumV(b),s2) => (NumV(a*b),s2)
      case _ => sys.error("Can only multiply numbers")
    }
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => eval(f,env,s) match {
    case (ClosureV(Fun(p,b),cEnv),s1) => eval(a,env,s1) match {
      case (v,s2) => eval(b, cEnv+(p -> v), s2)
    }
    case _ => sys.error("Can only apply functions")
  }
  case If0(c,t,f) => eval(c,env,s) match {
    case (NumV(0),s1) => eval(t,env,s1)
    case (NumV(_),s1) => eval(f,env,s1)
    case _ => sys.error("Can only check if number is zero")
  }
  case Seq(e1,e2) =>
    eval(e2,env,eval(e1,env,s)._2)
  case NewBox(e) => eval(e,env,s) match {
    case (v,s1) =>
      val a = nextAddress
      (AddressV(a), s1+(a -> v))
  }
  case SetBox(b,e) => eval(b,env,s) match {
    case (AddressV(a),s1) => eval(e,env,s1) match {
      case (v,s2) => (v, s2+(a -> v))
    }
    case _ => sys.error("Can only set boxes")
  }
  case OpenBox(b) =>  eval(b,env,s) match {
    case (AddressV(a),s1) => (s1(a),s1)
    case _ => sys.error("Can only open boxes")
  }
}


val ex1 =
  wth("b", NewBox(0), Seq( SetBox("b", Add(1,OpenBox("b"))), OpenBox("b") ))
assert(eval(ex1,Map(),Map())._1 == NumV(1))
/** With b = NewBox(0):
 *    SetBox(b, 1+OpenBox(b));
 *    OpenBox(b);
 */

val ex2 = wth("b", NewBox(1), wth("f", Fun("x", Add("x", OpenBox("b"))), Seq(SetBox("b",2), App("f",5))))
assert(eval(ex2,Map(),Map())._1 == NumV(7))
/** With b = NewBox(1):
 *    With f = (x => x+OpenBox(b):
 *       SetBox(b, 2);
 *       f(5);
 */

val ex3 =
  wth("switch", NewBox(0),
    wth("toggle", Fun("dummy",
      If0(OpenBox("switch"),
        Seq(SetBox("switch",1), 1),
        Seq(SetBox("switch",0), 0))),
      Add(App("toggle",42), App("toggle",42))))
assert(eval(ex3,Map(),Map())._1 == NumV(1))


