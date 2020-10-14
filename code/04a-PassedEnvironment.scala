import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp // switched from Symbol to String - no more warnings!
case class With(x: String, xdef: Exp, body: Exp) extends Exp
case class Call(f: String, args: List[Exp]) extends Exp // Num as type for args?

// function definitions and global map from identifiers to definitions
case class FunDef(args: List[String], body: Exp)
type Funs = Map[String, FunDef]

type Env = Map[String, Int]

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

def subst(body: Exp, i: String, v: Num) : Exp = body match {
  case Num(_) => body
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else body
  case With(x,xdef,body) =>
    With(x, subst(xdef,i,v), if (x == i) body else subst(body,i,v))
  case Call(f,args) => Call(f, args.map(subst(_,i,v)))
}

def evalWithEnv(funs: Funs, env: Env, e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => evalWithEnv(funs,env,l) + evalWithEnv(funs,env,r)
  case Mul(l,r) => evalWithEnv(funs,env,l) * evalWithEnv(funs,env,r)
  case Id(x) => env(x)
  case With(x,xdef,body) =>
    evalWithEnv(funs, env+(x -> evalWithEnv(funs,env,xdef)), body)
  case Call(f,args) =>
    val fDef = funs(f)
    val vArgs = args.map(evalWithEnv(funs,env,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f)
    // ++ operator adds all tuples in a list to a map
    // evalWithEnv(funs, env++fDef.args.zip(vArgs), fDef.body) // dynamic scoping
    evalWithEnv(funs, Map()++fDef.args.zip(vArgs), fDef.body) // lexical scoping
}

def evalWithSubst(funs: Funs, e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => evalWithSubst(funs,l) + evalWithSubst(funs,r)
  case Mul(l,r) => evalWithSubst (funs,l) * evalWithSubst(funs,r)
  case With(x,xdef,body) => evalWithSubst(funs, subst(body,x,Num(evalWithSubst(funs,xdef))))
  case Call(f,args) =>
    val fDef = funs(f)
    val vArgs = args.map(evalWithSubst(funs,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f)
    val substBody = fDef.args.zip(vArgs).foldLeft(fDef.body)( (b,av) => subst(b, av._1, Num(av._2)) )
    // Zip list of symbols and list of integers, fold resulting list of tuples.
    // Begin with body, apply subst function for each tuple in zipped list.
    // b is preliminary body (still missing substitutions), av is tuple of parameter name and
    // corresponding value
    evalWithSubst(funs, substBody)
}

// test functions
val fm = Map("square" -> FunDef(List("x"), Mul("x","x")),
             "succ" -> FunDef(List("x"), Add("x",1)),
             "myAdd" -> FunDef(List("x","y"), Add("x","y")),
             "forever" -> FunDef(List("x"), Call("forever", List("x"))))
// test expressions
val a = Call("square", List(Add(1,3)))
assert(evalWithSubst(fm,a) == 16)
assert(evalWithEnv(fm, Map(), a) == 16)
val b = Mul(2, Call("succ", List(Num(20))))
assert(evalWithSubst(fm,b) == 42)
assert(evalWithEnv(fm, Map(), b) == 42)
val c = Call("myAdd", List(Num(40), Num(2)))
assert(evalWithSubst(fm,c) == 42)
assert(evalWithEnv(fm, Map(), c) == 42)

// does not terminate
val forever = Call("forever", List(Num(0)))

// scoping example - causes error with lexical scoping, evaluates to 3 with dynamic scoping
val exFunMap = Map("f" -> FunDef(List("x"), Add("x","y")))
val exExpr = With("y", 1, Call("f", List(2)))

