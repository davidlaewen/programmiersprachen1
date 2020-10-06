import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Id(name: String) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)

abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class FunV(f: Value => Value) extends Value


case class Visitor[T](
  num: Int        => T,
  id : String     => T,
  add: (T,T)      => T,
  fun: (String,T) => T,
  app: (T,T)      => T
)


def foldExp[T](v: Visitor[T], e: Exp) : T = e match {
  case Num(n) => v.num(n)
  case Id(x) => v.id(x)
  case Add(l,r) => v.add(foldExp(v,l), foldExp(v,r))
  case Fun(p,b) => v.fun(p, foldExp(v,b))
  case App(f,a) => v.app(foldExp(v,f), foldExp(v,a))
}


val evalVisitor = new Visitor[Env => Value](
  n     => _   => NumV(n),
  x     => env => env(x),
  (l,r) => env => (l(env),r(env)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  },
  (p,b) => env => FunV(x => b(env+(p -> x))),
  (f,a) => env => (f(env),a(env)) match {
    case (FunV(g),arg) => g(arg)
    case _ => sys.error("Can only apply functions")
  })

val countVisitor = new Visitor[Int](_ => 1, _ => 1, (l,r) => l+r, (_,b) => 1+b, (f,a) => f+a)
val printVisitor = new Visitor[String](
  n => n.toString, id => id, (l,r) => l+"+"+r, (p,b) => "λ"+p+"."+b, (f,a) => "("+f+" "+a+")"
)

val ex1 = App(Fun("n", Add("n",1)), Add(2,1))
val ex2 = App(Fun("a", App(Fun("b", App(Fun("f", App("f", Add("a","b"))), Fun("n",Add("n",1)))), 4)), 5)


