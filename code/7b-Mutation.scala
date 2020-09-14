import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
case class If0(cond: Exp, thenExpr: Exp, elseExpr: Exp) extends Exp
case class Letrec(x: String, xDef: Exp, body: Exp) extends Exp

object Values {
  trait ValueHolder {
    def value: Value
  }
  sealed abstract class Value extends ValueHolder {
    def value : Value = this
  }
  case class ValuePointer(var v: Value) extends ValueHolder {
    def value : Value = v
  }
  case class NumV(n: Int) extends Value
  case class ClosureV(f: Fun, env: Env) extends Value
  type Env = Map[String,ValueHolder]
}

import Values._

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)