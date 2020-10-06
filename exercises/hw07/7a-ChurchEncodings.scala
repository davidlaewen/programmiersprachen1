import scala.language.implicitConversions

sealed abstract class Exp
case class Id(name: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp
case class printDot() extends Exp

implicit def string2exp(s: String): Id = Id(s)

abstract class Value
type Env = Map[String, Value]
case class ClosureV(f: Fun, env: Env) extends Value

def eval(e: Exp, env: Env) : Value = e match {
  case Id(x) => env(x)
  case f@Fun(_,_) => ClosureV(f,env)
  case App(f,a) => eval(f,env) match {
    case ClosureV(Fun(p,b),cEnv) => eval(b,cEnv+(p -> eval(a,env)))
  }
  case printDot() => print("."); ClosureV(Fun("x","x"), Map.empty)
}

// Booleans
val t = Fun("t", Fun("f","t"))
val f = Fun("t", Fun("f","f"))

val or = Fun("a", Fun("b", App(App("a", t), "b")))
val and = Fun("a", Fun("b", App(App("a", "b"), f)))

// Numbers
val zero = Fun("s", Fun("z","z"))
val succ = Fun("n", Fun("s", Fun("z", App("s", App(App("n", "s"), "z")))))
val one = App(succ,zero)
val two = App(succ,one)
val three = App(succ,two)

val printNumber = Fun("n", App(App("n", Fun("x",printDot())), zero))
def printNum(e: Exp) : Value = eval(App(printNumber,e), Map())

val add =
  Fun("a", Fun("b", Fun("s", Fun("z",
    App(App("a","s"), App(App("b","s"),"z"))))))

// Lists
val empty = Fun("c", Fun("e","e"))
val cons =
  Fun("h", Fun("r", Fun("c", Fun("e",
    App(App("c","h"), App(App("r","c"), "e"))))))

val list123 = App(App(cons,one), App(App(cons,two), App(App(cons,three), empty)))
val list232 = App(App(cons,two), App(App(cons,three), App(App(cons,two), empty)))
val list000 = App(App(cons,zero), App(App(cons,zero), App(App(cons,zero), empty)))
val list101 = App(App(cons,one), App(App(cons,zero), App(App(cons,one), empty)))

/** Task 1 */
val mul =
  Fun("a", Fun("b",
    App(App("b", App(add, "a")), zero))) // fold b with (_ => a+_) and zero

/** Task 2 */
val sumList = Fun("l", App(App("l",add), zero))

/** Task 3 */
val isZero = Fun("n", App(App("n", Fun("x",f)),t))
// val foldFun = Fun("h", Fun("t", App(App(and, App(isZero, "h")), "t"))) // And(isZero(head), tail)
val foldFun = Fun("h", Fun("t", App(App(App(isZero, "h"), "t"), f))) // If isZero(head) Then tail Else false
val allZeros = Fun("l", App(App("l",foldFun), t))

assert(eval(App(allZeros,list123), Map()).asInstanceOf[ClosureV].f == f)
assert(eval(App(allZeros,list101), Map()).asInstanceOf[ClosureV].f == f)
assert(eval(App(allZeros,list000), Map()).asInstanceOf[ClosureV].f == t)
assert(eval(App(allZeros,  empty), Map()).asInstanceOf[ClosureV].f == t)

