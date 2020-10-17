import scala.language.implicitConversions

sealed abstract class Type

/** Simply-Typed Lambda Calculus (STLC) */
sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

case class Fun(param: String, t: Type, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

/** Extended with Unit, Let, Type Ascription, Products and Sums */
case class JUnit() extends Exp
case class Let(x: String, xDef: Exp, body: Exp) extends Exp

case class TypeAscription(e: Exp, t: Type) extends Exp

case class Product(e1: Exp, e2: Exp) extends Exp
case class Fst(e: Exp) extends Exp
case class Snd(e: Exp) extends Exp

case class SumLeft(l: Exp, r: Type) extends Exp
case class SumRight(l: Type, r: Exp) extends Exp
case class EliminateSum(e: Exp, fl: Exp, fr: Exp) extends Exp


/** Interpreter */
def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names(freshName)) {
    freshName = default+last.toString
    last += 1
  }
  freshName
}

def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set()
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l)++freeVars(r)
  case Fun(p,_,b) => freeVars(b)-p
  case App(f,a) => freeVars(f)++freeVars(a)
  case JUnit() => Set()
  case Let(x,xDef,b) => freeVars(xDef) ++ (freeVars(b)-x)
  case TypeAscription(e,_) => freeVars(e)
  case Product(l,r) => freeVars(l)++freeVars(r)
  case Fst(e) => freeVars(e)
  case Snd(e) => freeVars(e)
  case SumLeft(l,_) => freeVars(l)
  case SumRight(_,r) => freeVars(r)
  case EliminateSum(e,fl,fr) => freeVars(e)++freeVars(fl)++freeVars(fr)
}

def subst(e: Exp, x: String, xDef: Exp) : Exp = e match {
  case Num(_) => e
  case Id(y) => if (x==y) xDef else e
  case Add(l,r) => Add(subst(l,x,xDef), subst(r,x,xDef))
  case Fun(p,t,b) => if (x==p) e else {
    val fVs = freeVars(b)++freeVars(xDef)
    val newVar = freshName(fVs,p)
    Fun(newVar,t,subst(subst(b,p,Id(newVar)),x,xDef))
  }
  case App(f,a) => App(subst(f,x,xDef), subst(a,x,xDef))
  case JUnit() => e
  case Let(y,yDef,b) =>
    if (x==y) Let(y,subst(yDef,x,xDef),b) else {
    val fVs = freeVars(b)++freeVars(xDef)
      val newVar = freshName(fVs,y)
      Let(newVar,subst(yDef,x,xDef),subst(subst(b,y,Id(newVar)),x,xDef))
  }
  case TypeAscription(e,t) => TypeAscription(subst(e,x,xDef),t)
  case Product(l,r) => Product(subst(l,x,xDef),subst(r,x,xDef))
  case Fst(e) => Fst(subst(e,x,xDef))
  case Snd(e) => Snd(subst(e,x,xDef))
  case SumLeft(l,t) => SumLeft(subst(l,x,xDef),t)
  case SumRight(t,r) => SumRight(t,subst(r,x,xDef))
  case EliminateSum(e,fl,fr) => EliminateSum(subst(e,x,xDef),subst(fl,x,xDef),subst(fr,x,xDef))
}

def eval(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: "+x)
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(p,_,b) => eval(subst(b,p,eval(a)))
    case _ => sys.error("Can only apply functions")
  }
  case Let(y,yDef,b) => eval(subst(b,y,yDef))
  case TypeAscription(e,_) => eval(e)
  case Product(l,r) => Product(eval(l),eval(r))
  case Fst(e) => eval(e) match {
    case Product(l,_) => eval(l)
    case _ => sys.error("Can only select first from products")
  }
  case Snd(e) => eval(e) match {
    case Product(_,r) => eval(r)
    case _ => sys.error("Can only select second from products")
  }
  case SumLeft(l,t) => SumLeft(eval(l),t)
  case SumRight(t,r) => SumRight(t,eval(r))
  case EliminateSum(e,fl,fr) => eval(e) match {
    case SumLeft(l,_) => eval(App(fl,l))
    case SumRight(_,r) => eval(App(fr,r))
    case _ => sys.error("Can only eliminate sums")
  }
  case _ => e
}


/** Type system */
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


val ex1 = Fun("x",NumType(),Add("x",1))
assert(typeCheck(ex1,Map()) == FunType(NumType(),NumType()))
val ex2 = Fun("x",NumType(),Fst("x"))
// typeCheck(ex2,Map()) throws "Type error in Fst: can only project products"
val ex3 = Fun("x",ProductType(NumType(),JUnitType()),Fst("x"))
assert(typeCheck(ex3,Map()) == FunType(ProductType(NumType(),JUnitType()),NumType()))
val ex4 = Let("x",Num(5),Add("x",1))
assert(typeCheck(ex4,Map()) == NumType())



