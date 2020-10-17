import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Bool(b: Boolean) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class If(c: Exp, t: Exp, f: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def bool2exp(b: Boolean) : Exp = Bool(b)

def eval(e: Exp) : Exp = e match {
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case If(c,t,f) => eval(c) match {
    case Bool(true) => eval(t)
    case Bool(false) => eval(f)
    case _ => sys.error("Condition must be a boolean")
  }
  case _ => e
}

val ex1 = Add(3,5)
val ex2 = If(true,7,5)
val ex3 = Add(5, If(true,7,true))
val ex4 = If(Add(3,4),7,true)
val ex5 = Add(true,3)


/** Type system */
sealed abstract class Type
case class BoolType() extends Type
case class NumType() extends Type

def typeCheck(e: Exp) : Type = e match {
  case Num(_) => NumType()
  case Bool(_) => BoolType()
  case Add(l,r) => (typeCheck(l),typeCheck(r)) match {
    case (NumType(),NumType()) => NumType()
    case _ => sys.error("Type error in Add")
  }
  case If(c,t,f) => (typeCheck(c),typeCheck(t),typeCheck(f)) match {
    case (BoolType(),tType,fType) =>
      if (tType == fType) tType else sys.error("Type error in If")
    case _ => sys.error("Type error in If")
  }
}


