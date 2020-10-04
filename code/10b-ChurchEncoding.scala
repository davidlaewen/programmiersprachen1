import scala.language.implicitConversions

sealed abstract class Exp
case class Id(name: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp
case class printDot() extends Exp // visualization for encoded numbers

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

/** Church encoding: */

/** Booleans: true(a,b) = a, false(a,b) = b */
val t = Fun("t", Fun("f","t")) // true
val f = Fun("t", Fun("f","f")) // false

val ifTE = Fun("c", Fun("t", Fun("e", App(App("c","t"),"e")))) // if c then t else e
val not = Fun("a", App(App("a", f), t)) // if a then False else True
// val not = Fun("a", App(App(App(ifTE, "a"),f),t))
val and = Fun("a", Fun("b", App(App("a", "b"), f))) // if a then b else False
val or = Fun("a", Fun("b", App(App("a", t), "b")))  // if a then True else b

assert(eval(App(App(App(ifTE, t),f),t), Map()) == ClosureV(f,Map())) // not true
assert(eval(App(App(App(ifTE, f),f),t), Map()) == ClosureV(t,Map())) // not false

/** Numbers: n = s(s(...(s(z)))) [n-fold application of s on z] */
val zero = Fun("s", Fun("z","z"))
val succ = Fun("n", Fun("s", Fun("z", App("s", App(App("n", "s"), "z"))))) // "unwrap & re-wrap"
val one = App(succ,zero) // = Fun("s", Fun("z", App("s","z")))
val two = App(succ,one) // = Fun("s", Fun("z", App("s", App("s","z"))))
val three = App(succ,two)

val printNum = Fun("n", App(App("n", Fun("x",printDot())), f)) // prints n dots for number n

val add =
  Fun("a", Fun("b", Fun("s", Fun("z",
    App(App("a","s"), App(App("b","s"),"z"))))))

val mul =
  Fun("a", Fun("b", Fun("s", Fun("z",
    App(App("a", App("b","s")), "z")))))

val isZero = Fun("n", App(App("a", Fun("x",f)),t)) // returns True if n=0, else n-fold application of λx.False

/** Lists: Comprised of 'empty' and 'cons' */
val empty = Fun("c", Fun("e","e")) // equivalent to 0 and False
val cons =
  Fun("h", Fun("r", Fun("c", Fun("e", // "re-wrap" the result
    App(App("c","h"), App(App("r","c"), "e")))))) // apply c to head and unpacked rest

val list123 = App(App(cons,one), App(App(cons,two), App(App(cons,three), empty)))

val listSum = Fun("l", App(App("l",add), zero))
 




