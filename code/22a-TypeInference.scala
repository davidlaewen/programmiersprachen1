import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(x: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp
case class Let(x: String, xDef: Exp, body: Exp) extends Exp

implicit def int2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)


/** Substitution for Let polymorphism */
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
  case Fun(p,b) => freeVars(b)-p
  case App(f,a) => freeVars(f)++freeVars(a)
  case Let(x,xDef,b) => freeVars(xDef) ++ (freeVars(b)-x)
}
def subst(e: Exp, x: String, xDef: Exp) : Exp = e match {
  case Num(_) => e
  case Id(y) => if (x==y) xDef else e
  case Add(l,r) => Add(subst(l,x,xDef), subst(r,x,xDef))
  case Fun(p,b) => if (x==p) e else {
    val fVs = freeVars(b)++freeVars(xDef)
    val newVar = freshName(fVs,p)
    Fun(newVar,subst(subst(b,p,Id(newVar)),x,xDef))
  }
  case App(f,a) => App(subst(f,x,xDef), subst(a,x,xDef))
  case Let(y,yDef,b) =>
    if (x==y) Let(y,subst(yDef,x,xDef),b) else {
    val fVs = freeVars(b)++freeVars(xDef)
      val newVar = freshName(fVs,y)
      Let(newVar,subst(yDef,x,xDef),subst(subst(b,y,Id(newVar)),x,xDef))
  }
}


/** Type system */
sealed abstract class Type
case class FunType(from: Type, to: Type) extends Type
case class NumType() extends Type
case class TypeVar(x: String) extends Type

/** Type checker (generates list of constraints) */
var typeVarCount : Int = 0
def freshTypeVar() : Type = {
  typeVarCount += 1
  TypeVar("X"+typeVarCount.toString)
}

def typeCheck(e: Exp, gamma: Map[String,Type]) : (List[(Type,Type)],Type) = e match {
  case Num(_) => (List(),NumType())
  case Id(x) => gamma.get(x) match {
    case Some(t) => (List(),t)
    case _ => sys.error("Unbound identifier: "+x)
  }
  case Add(l,r) => (typeCheck(l,gamma),typeCheck(r,gamma)) match {
    case ((lEqs,lt),(rEqs,rt)) => (lt->NumType() :: rt->NumType() :: lEqs++rEqs, NumType())
  }
  case Fun(p,b) =>
    val xt = freshTypeVar()
    val resBody = typeCheck(b,gamma+(p->xt))
    (resBody._1, FunType(xt,resBody._2))
  case App(f,a) =>
    val toType = freshTypeVar()
    (typeCheck(f,gamma),typeCheck(a,gamma)) match {
      case ((fEqs, ft), (aEqs, at)) => ((ft, FunType(at, toType)) :: fEqs ++ aEqs, toType)
    }
  // Let polymorphism:
  case Let(x,xDef,b) =>
    val (eqs1,_) = typeCheck(xDef,gamma)
    val (eqs2,t) = typeCheck(subst(b,x,xDef),gamma)
    (eqs1++eqs2,t)
}


/** Robinson Unification Algorithm (solves system of equations) */
def substitution(x: String, s: Type): Type => Type = new Function[Type,Type] {
  def apply(t: Type) : Type = t match {
    case FunType(from, to) => FunType(this(from), this(to))
    case NumType() => NumType()
    case TypeVar(y) => if (x==y) s else t
  }
}

def freeTypeVars(t: Type) : Set[String] = t match {
  case FunType(f,t) => freeTypeVars(f)++freeTypeVars(t)
  case NumType() => Set()
  case TypeVar(x) => Set(x)
}

def unify(eq: List[(Type,Type)]) : Type => Type = eq match {
  case List() => identity
  case (NumType(),NumType())::rest => unify(rest)
  case (FunType(f1,t1),FunType(f2,t2))::rest => unify(f1->f2::t1->t2::rest)
  case (TypeVar(x1),TypeVar(x2))::rest if x1==x2 => unify(rest)
  case (TypeVar(x),t)::rest =>
    if (freeTypeVars(t)(x)) sys.error(s"Occurs check: $x occurs in $t")
    val s = substitution(x,t)
    s.andThen(unify(rest.map(p => (s(p._1),s(p._2)))))
  case (t,TypeVar(x))::rest => unify((TypeVar(x),t)::rest)
  case (t1,t2)::_ => sys.error(s"Cannot unify $t1 and $t2")
}


/** Full type checker */
def doTypeCheck(e: Exp): Type = {
  val (eqs,t) = typeCheck(e,Map())
  unify(eqs)(t)
}

/** Example expressions */
val ex1 = Fun("x",Add("x",1))
// ~~> type checks with FunType(NumType(),NumType())

val ex2 = Fun("x", Add("x",App("x",5)))
// ~~> NumType() and FunType(NumType(),NumType()) cannot be unified



