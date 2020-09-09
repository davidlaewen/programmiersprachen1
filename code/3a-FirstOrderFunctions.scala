import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: Symbol) extends Exp
case class With(x: Symbol, xdef: Exp, body: Exp) extends Exp
case class Call(f: Symbol, args: List[Exp]) extends Exp // Num as type for args?

case class FunDef(args: List[Symbol], body: Exp)

// global map from function names to definitions
type Funs = Map[Symbol, FunDef]

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(Symbol(s))
def sym(s: String) : Symbol = Symbol(s)

def subst(body: Exp, i: Symbol, v: Num) : Exp = body match {
  case Num(n) => body
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else body
  case With(x,xdef,body) =>
    With(x, subst(xdef,i,v), if (x == i) body else subst(body,i,v))
  case Call(f,args) => Call(f, args.map(subst(_,i,v)))
}

def eval(funs: Funs, e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x.name)
  case Add(l,r) => eval(funs,l) + eval(funs,r)
  case Mul(l,r) => eval(funs,l) * eval(funs,r)
  case With(x,xdef,body) => eval(funs, subst(body,x,Num(eval(funs,xdef))))
  case Call(f,args) => {
    val fDef = funs(f)
    val vArgs = args.map(eval(funs,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f.name)
    val substBody = fDef.args.zip(vArgs).foldLeft(fDef.body)( (b,av) => subst(b, av._1, Num(av._2)) )
    // Zip list of symbols and list of integers, fold resulting list of tuples.
    // Begin with body, apply subst function for each tuple in zipped list.
    // b is preliminary body (still missing substitutions), av is tuple of parameter name and
    // corresponding value
    eval(funs, substBody)
  }
}

// test functions
val fm = Map(sym("square") -> FunDef(List(sym("x")), Mul("x","x")),
             sym("succ") -> FunDef(List(sym("x")), Add("x",1)),
             sym("myAdd") -> FunDef(List(sym("x"),sym("y")), Add("x","y")),
             sym("forever") -> FunDef(List(sym("x")), Call(sym("forever"), List("x"))))
// test expressions
val a = Call(sym("square"), List(Add(1,3)))
assert(eval(fm,a) == 16)
val b = Mul(2, Call(sym("succ"), List(Num(20))))
assert(eval(fm,b) == 42)
val c = Call(sym("myAdd"), List(Num(40), Num(2)))
assert(eval(fm,c) == 42)

// does not terminate
val forever = Call(sym("forever"), List(Num(0)))

