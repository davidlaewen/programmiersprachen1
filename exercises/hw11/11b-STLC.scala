import scala.language.implicitConversions


/** STLC language */
sealed abstract class Type

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

case class Fun(param: String, from: Type, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

case class JUnit() extends Exp
case class Let(x: String, xDef: Exp, body: Exp) extends Exp
case class TypeAscription(e: Exp, t: Type) extends Exp
case class Product(e1: Exp, e2: Exp) extends Exp
case class Fst(e: Exp) extends Exp
case class Snd(e: Exp) extends Exp
case class SumLeft(l: Exp, r: Type) extends Exp
case class SumRight(l: Type, r: Exp) extends Exp
case class EliminateSum(e: Exp, fl: Exp, fr: Exp) extends Exp

/** STLC type system */
case class NumType() extends Type
case class FunType(from: Type, to: Type) extends Type
case class JUnitType() extends Type
case class ProductType(l: Type, r: Type) extends Type
case class SumType(l: Type, r: Type) extends Type

def typeCheck(e: Exp, gamma: Map[String,Type]) : Type = e match {
  case Num(_) => NumType()
  case Id(x) => gamma.get(x) match {
    case Some(t) => t
    case _ => sys.error("Type error: Unbound identifier "+x)
  }
  case Add(l,r) => (typeCheck(l,gamma),typeCheck(r,gamma)) match {
    case (NumType(),NumType()) => NumType()
    case _ => sys.error("Type error in Add: can only add numbers")
  }
  case Fun(p,t,b) => FunType(t, typeCheck(b,gamma+(p -> t)))
  case App(f,a) => typeCheck(f,gamma) match {
    case FunType(from,to) =>
      if (from == typeCheck(a,gamma)) to
      else sys.error("Type error in App: arg does not match expected type")
    case _ => sys.error("Type error in App: left expression must be a function")
  }
  case JUnit() => JUnitType()
  case Let(x,xDef,b) => typeCheck(b,gamma+(x -> typeCheck(xDef,gamma)))
  case TypeAscription(e,t) =>
    if (typeCheck(e,gamma)==t) t
    else sys.error("Type error in TypeAscription: type does not match")
  case Product(l,r) => ProductType(typeCheck(l,gamma),typeCheck(r,gamma))
  case Fst(e) => typeCheck(e,gamma) match {
    case ProductType(l,_) => l
    case _ => sys.error("Type error in Fst: can only project products")
  }
  case Snd(e) => typeCheck(e,gamma) match {
    case ProductType(_,r) => r
    case _ => sys.error("Type error in Snd: can only project products")
  }
  case SumLeft(l,t) => SumType(typeCheck(l,gamma),t)
  case SumRight(t,r) => SumType(t,typeCheck(r,gamma))
  case EliminateSum(c,fl,fr) => typeCheck(c,gamma) match {
    case SumType(l,r) => (typeCheck(fl,gamma),typeCheck(fr,gamma)) match {
      case (FunType(lFrom,lTo),FunType(rFrom,rTo)) if l==lFrom && r==rFrom && lTo==rTo =>
        if (lTo==rTo) lTo
        else sys.error("Type error in EliminateSum: functions must have same return type")
      case _ => sys.error("Type error in EliminateSum: 2nd and 3rd arg must be functions")
    }
    case _ => sys.error("Type error in EliminateSum: can only eliminate sums")
  }
}


/** Task 1 */
val plusOneOpen: Exp = Add("x",1)
val plusOneGamma: Map[String,Type] = Map("x" -> NumType())
// type checks with type NumType()

// val plusScala = x + 1
// does not type check, x not bound

def plusOneOpenScala(x: Int): Int = x+1 // context represented by parameter
// type checks with return type Int

/** Task 2.1 */
val e1: Exp = Fun("x", NumType(), "x")
// type checks with type FunType(NumType(),NumType())

// val e1Scala = (x: Int) => x
// type checks with type Int => Int

/** Task 2.2 */
val boolT = SumType(JUnitType(),JUnitType())
val e2: Exp = Fun("x", boolT, "x")
// type checks with type FunType(boolT,boolT) a.k.a.
// FunType(SumType(JUnitType(),JUnitType()), SumType(JUnitType(),JUnitType()))

val e2Scala = (x: Boolean) => x
// type checks with type Boolean => Boolean

/** Task 2.3 */
val e3: Exp = Fun("x", FunType(NumType(), NumType()), "x")
// type checks with type
// FunType( FunType(NumType(), NumType()), FunType(NumType(), NumType()) )

val e3Scala = (x: (Int => Int)) => x
// type checks with type (Int => Int) => (Int => Int)

/** Task 2.4 */
val funPlusOne: Exp = Fun("x", FunType(NumType(), NumType()), Add("x", 1))
// throws error with message "Type error in Add: can only add numbers"

// val funPlusOneScala = (x: (Int => Int)) => x + 1
// throws type mismatch error since (Int => Int) is not compatible with '+'



