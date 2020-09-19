import scala.language.implicitConversions

sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp
case class Id(x: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
case class If0(cond: Exp, thenExpr: Exp, elseExpr: Exp) extends Exp

case class NewBox(e: Exp) extends Exp
case class SetBox(b: Exp, e: Exp) extends Exp
case class OpenBox(b: Exp) extends Exp
case class Seq(e1: Exp, e2: Exp) extends Exp


implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s)
def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)


sealed abstract class Value {
  var marked : Boolean = false
}
type Env = Map[String,Value] // scala.collection.mutable.Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value
case class AddressV(a: Int) extends Value
trait Store {
  // add v to store, return address, perform GC if necessary
  def malloc(stack: List[Env], v: Value) : Int
  // update the value at index to v (by mutating the store)
  def update(index: Int, v: Value) : Unit
  // fetch value at index
  def apply(index: Int) : Value
}


def eval(e: Exp, stack: List[Env], store: Store) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => stack.head(x)
  case Add(l,r) => (eval(l,stack,store),eval(r,stack,store)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l,stack,store),eval(r,stack,store)) match {
    case (NumV(a),NumV(b)) => NumV(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case f@Fun(_,_) => ClosureV(f,stack.head)
  case If0(c,t,f) => eval(c,stack,store) match {
    case NumV(0) => eval(t,stack,store)
    case NumV(_) => eval(f,stack,store)
    case _ => sys.error("Can only check if number is zero")
  }
  case App(f,a) => eval(f,stack,store) match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(b, cEnv+(p -> eval(a,stack,store))::stack, store)
    case _ => sys.error("Can only apply functions")
  }
  case Seq(e1,e2) => eval(e1,stack,store); eval(e2,stack,store)
  case NewBox(e: Exp) =>
    val a = store.malloc(stack,eval(e,stack,store))
    AddressV(a)
  case SetBox(b: Exp, e: Exp) => eval(b,stack,store) match {
    case AddressV(a) =>
      val v = eval(e,stack,store)
      store.update(a,v)
      v
    case _ => sys.error("Can only set boxes")
  }
  case OpenBox(b: Exp) => eval(b,stack,store) match {
    case AddressV(a) => store(a) // == store.apply(a)
    case _ => sys.error("Can only open boxes")
  }
}

// "stupid" Store implementation with no GC
class StoreNoGC(size: Int) extends Store {
  val memory = new Array[Value](size)
  var nextFreeAddr: Int = 0
  def malloc(stack: List[Env], v: Value) : Int = {
    val a = nextFreeAddr
    if (a >= size) sys.error("Out of memory!")
    nextFreeAddr += 1; update(a,v); a
  }
  def update(index: Int, v: Value) : Unit = memory.update(index,v)
  def apply(index: Int) : Value = memory(index)
}



val ex1 =
  wth("switch", NewBox(0),
    wth("toggle", Fun("dummy",
      If0(OpenBox("switch"),
        Seq(SetBox("switch",1), 1),
        Seq(SetBox("switch",0), 0))),
      Add(App("toggle",42), App("toggle",42))))



