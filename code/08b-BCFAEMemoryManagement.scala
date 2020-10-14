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
  // update the value at index i to v (by mutating the store)
  def update(i: Int, v: Value) : Unit
  // fetch value at index i
  def apply(i: Int) : Value
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

/**
 * "stupid" Store implementation with no GC
 */
class StoreNoGC(size: Int) extends Store {
  val memory = new Array[Value](size)
  var nextFreeAddr: Int = 0
  def malloc(stack: List[Env], v: Value) : Int = {
    val a = nextFreeAddr
    if (a >= size) sys.error("Out of memory!")
    nextFreeAddr += 1; update(a,v); a
  }
  def update(i: Int, v: Value) : Unit = { memory(i) = v }
  def apply(i: Int) : Value = memory(i)
}

val ex1 =
  wth("b", NewBox(0),
    Seq( SetBox("b", Add(1,OpenBox("b"))), OpenBox("b") ))

val store = new StoreNoGC(2)
val stack = List[Env]()
eval(ex1,stack,store)
eval(ex1,stack,store) // third call would cause error

/**
 * Store implementation with Mark & Sweep GC
 */
class MarkAndSweepStore(size: Int) extends Store {
  val memory = new Array[Value](size)
  var free : Int = size
  var nextFreeAddr : Int = 0
  def malloc(stack: List[Env], v: Value) : Int = {
    if (free <= 0) gc(stack)
    if (free <= 0) sys.error("Out of memory!")
    while (memory(nextFreeAddr) != null) {
      nextFreeAddr += 1
      if (nextFreeAddr == size) nextFreeAddr = 0
    }
    update(nextFreeAddr,v); free -= 1; nextFreeAddr
  }
  def update(i: Int, v: Value) : Unit = { memory(i) = v }
  def apply(i: Int) : Value = memory(i)

  // Mark & Sweep GC:
  def allAddrInVal(v: Value) : Set[Int] = v match {
    case NumV(_) => Set()
    case AddressV(a) => Set(a)
    case ClosureV(_,env) => allAddrInEnv(env)
  }
  def allAddrInEnv(env: Env) : Set[Int] = {
    env.values.map{allAddrInVal}.fold(Set())(_++_)
  }
  def mark(seed: Set[Int]) : Unit = {
    seed.foreach(memory(_).marked = true)
    val newAddresses =
      seed.flatMap(a => allAddrInVal(memory(a))).filter(!memory(_).marked)
    if (newAddresses.nonEmpty) {
      mark(newAddresses)
    }
  }
  def sweep() : Unit = {
    memory.indices.foreach(
      i => if (memory(i) == null) {}
      else if (memory(i).marked) memory(i).marked = false
      else { memory(i) = null; free += 1 }
    )
  }
  def gc(stack: List[Env]) : Unit = {
    mark(stack.map(allAddrInEnv).fold(Set())(_++_))
    sweep()
  }
}






