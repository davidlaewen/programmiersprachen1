import scala.language.implicitConversions

trait Exp[T] {
  implicit def num(n: Int) : T
  implicit def id(name: String) : T
  def add(l: T, r: T) : T
  def fun(param: String, body: T) : T
  def app(fun: T, arg: T) : T
  def wth(x: String, xDef: T, body: T) : T = app(fun(x,body),xDef)
}

sealed abstract class Value
type Env = Map[String,Value]
case class ClosureV(f: Value => Value) extends Value
case class NumV(n: Int) extends Value

trait Eval extends Exp[Env => Value] {
  def num(n: Int) : Env => Value = _ => NumV(n)
  def id(x: String) : Env => Value = env => env(x)
  def add(l: Env => Value, r: Env => Value) : Env => Value =
    env => (l(env),r(env)) match {
      case (NumV(a),NumV(b)) => NumV(a+b)
      case _ => sys.error("Can only add numbers")
    }
  def fun(p: String, b: Env => Value) : Env => Value =
    env => ClosureV(v => b(env+(p -> v)))
  def app(fun: Env => Value, arg: Env => Value) : Env => Value =
    env => fun(env) match {
      case ClosureV(f) => f(arg(env))
      case _ => sys.error("Can only apply functions")
    }
}

object eval extends Eval

def test[T](semantics: Exp[T]): T = {
  import semantics._
  app(app(fun("x",fun("y",add("x","y"))),5),3)
}
assert(test(eval)(Map()) == NumV(8))


/** Modular extension with new language feature */
trait ExpWithMul[T] extends Exp[T] {
  def mul(l: T, r: T) : T
}

object evalWithMul extends Eval with ExpWithMul[Env => Value] {
  def mul(l: Env => Value, r: Env => Value) : Env => Value =
    env => (l(env),r(env)) match {
      case (NumV(a),NumV(b)) => NumV(a*b)
      case _ => sys.error("Can only multiply numbers")
    }
}

def test2[T](semantics: ExpWithMul[T]) = {
  import semantics._
  app(app(fun("x",fun("y",mul("x","y"))),5),3)
}
assert(test2(evalWithMul)(Map()) == NumV(15))


/** Modular extension with new function */
trait PrintExp extends Exp[String] {
  def num(n: Int) : String = n.toString
  def id(x: String) : String = x
  def add(l: String, r: String) : String = l+"+"+r
  def fun(p: String, b: String) : String = "λ"+p+"."+"("+b+")"
  def app(f: String, a: String) : String = "("+f+" "+a+")"
}

object printExp extends PrintExp

object printExpWithMul extends PrintExp with ExpWithMul[String] {
  override def add(l: String, r: String) : String = "("+l+"+"+r+")"
  def mul(l: String, r: String) : String = l+"*"+r
}
