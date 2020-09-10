import scala.language.implicitConversions

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Bool(b: Boolean) extends Exp
case class Id(name: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
case class If(cond: Exp, thenBranch: Exp, elseBranch: Exp) extends Exp
case class Eq(lhs: Exp, rhs: Exp) extends Exp

implicit def num2exp(n: Int): Num = Num(n)
implicit def bool2exp(b: Boolean): Bool = Bool(b)
implicit def id2exp(s: String): Id = Id(s)

def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body),xDef)

def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names.contains(freshName)) {
    freshName = default+last
    last += 1
  }
  freshName
}

assert( freshName(Set("y","z"),"x") == "x")
assert( freshName(Set("x2","x0","x4","x","x1"),"x") == "x3")

def freeVars(e: Exp) : Set[String] =  e match {
  case Num(_) => Set.empty
  case Bool(_) => Set.empty
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Fun(x,body) => freeVars(body) - x
  case If(c,t,f) => freeVars(c) ++ freeVars(t) ++ freeVars(f)
  case Eq(l,r) => freeVars(l) ++ freeVars(r)
}

assert(freeVars(Fun("x",Add("x","y"))) == Set("y"))

def subst(e : Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Bool(_) => e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else e
  case App(f,a) => App(subst(f,i,v), subst(a,i,v))
  case Fun(param,body) =>
    if (param == i) e else {
      val fvs = freeVars(e) ++ freeVars(v) + i
      val newVar = freshName(fvs, param)
      Fun(newVar, subst(subst(body, param, Id(newVar)), i, v))
    }
  case If(c,t,f) => If(subst(c,i,v),subst(t,i,v),subst(f,i,v))
  case Eq(l,r) => Eq(subst(l,i,v), subst(r,i,v))
}

assert( subst(Add(5,"x"), "x", 7) == Add(5, 7))
assert( subst(Add(5,"x"), "y", 7) == Add(5,"x"))
assert( subst(Fun("x", Add("x","y")), "x", 7) == Fun("x", Add("x","y")))
// test capture-avoiding substitution
assert( subst(Fun("x", Add("x","y")), "y", Add("x",5)) == Fun("x0",Add(Id("x0"),Add(Id("x"),Num(5)))))
assert( subst(Fun("x", Add(Id("x0"), Id("y"))), "y", Add(Id("x"), 5)) == Fun("x1", Add(Id("x0"), Add(Id("x"), Num(5)))) )


def eval(e: Exp) : Exp = e match {
  case Id(v) => sys.error("Unbound identifier: " + v.name)
  case Add(l,r) => (eval(l), eval(r)) match {
    case (Num(x),Num(y)) => Num(x+y)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(x,body) => eval( subst(body,x, eval(a)))  // call-by-value
    // case Fun(x,body) => eval( subst(body,x, a))        // call-by-name
    case _ => sys.error("Can only apply functions")
  }
  case If(c,t,f) => eval(c) match {
    case Bool(b) => if (b) eval(t) else eval(f)
    case _ => sys.error("Condition must evaluate to boolean")
  }
  case Eq(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Bool(a == b)
    case (Bool(a),Bool(b)) => Bool(a == b)
    case _ => sys.error("Can only compare Num and Bool expressions")
  }
  case _ => e // numbers and functions evaluate to themselves
}

/**
def eval2(e: Exp) : Either[Num,Fun] = e match {
  case Id(v) => sys.error("unbound identifier: " + v.name)
  case Add(l,r) => (eval2(l), eval2(r)) match {
    case (Left(Num(x)),Left(Num(y))) => Left(Num(x+y))
    case _ => sys.error("can only add numbers")
  }
  case App(f,a) => eval2(f) match {
    case Right(Fun(x,body)) => eval2( subst(body,x, eval(a)))
    case _ => sys.error("can only apply functions")
  }
  case f@Fun(_,_) => Right(f)
  case n@Num(_) => Left(n)
}
 */

val test = App(Fun("x",Add("x",5)), 7)
assert(eval(test) == Num(12))
val ex1 = If(true, 4, 2)
assert(eval(ex1) == Num(4))
val ex2 = If(false, 4, 2)
assert(eval(ex2) == Num(2))
val ex3 = Add(1, If(false, 1, Add(1,2)))
assert(eval(ex3) == Num(4))
val ex4 = App(Fun("x",If("x",1,0)),true)
assert(eval(ex4) == Num(1))
val ex5 = App(Fun("x",If("x",false,true)),false)
assert(eval(ex5) == Bool(true))
val ex6 = Eq(Num(4),Add(2,2))
assert(eval(ex6) == Bool(true))
val ex7 = If(Eq(true,false),Num(1),Num(2))
assert(eval(ex7) == Num(2))

/**
 * 1.
 * The first possible behaviour is that the condition is evaluated first and then only the appropriate branch is
 * evaluated ("lazy", more prevalent, implemented above). The other behaviour would be that both branches are always
 * evaluated before the condition (requires more evaluation work, produces different results in languages with "side
 * effects"). In our language both implementations behave identically, making the first option superior
 *
 * 2.
 * Further features that would increase the usefulness of If are comparison constructs for Num (and Bool) values which
 * evaluate to Bool values (i.e. a '=', '>' and '<' construct). This would facilitate termination conditions in
 * recursive functions. Boolean operations such as 'And' and 'Or' would be a useful addition to the Bool construct and
 * would allow combining of conditions in If-expressions.
  */





