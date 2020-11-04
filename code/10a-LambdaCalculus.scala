import scala.language.implicitConversions

sealed abstract class Exp
case class Id(name: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp

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
}





