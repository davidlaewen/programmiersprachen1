import scala.language.implicitConversions
import scala.language.reflectiveCalls

trait Monad {
  type M[_] // abstract type member instead of abstract type parameter
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  def unit[A](a: A) : M[A]

  implicit def monadicSyntax[A](m: M[A]) = new {
    def map[B](f: A => B) : M[B] = bind(m, (x: A) => unit(f(x)))
    def flatMap[B](f: A => M[B]) : M[B] = bind(m,f)
  }
}


trait ReaderMonad extends Monad {
  type R
  def ask: M[R]
  def local[A](f: R => R, a: M[A]) : M[A]
}

trait StateMonad extends Monad {
  type S
  def getState : M[S]
  def putState(s: S) : M[Unit]
}

trait ContinuationMonad extends Monad {
  def callCC[A,B](f: (A => M[B]) => M[A]) : M[A]
}

trait IdentityMonad extends Monad {
  type M[A] = A
  def unit[A](a: A) : M[A] = a
  def bind[A,B](m: M[A], f: A => M[B]) : M[B] = f(m)
}

object IdentityMonad extends IdentityMonad


/** Monad transformer: nested monads through inner monads */
trait MonadTransformer extends Monad {
   protected val m: Monad
}

trait ReaderT extends MonadTransformer with ReaderMonad {
  type R
  type M[X] = R => m.M[X]
  def unit[A](a: A) : M[A] = r => m.unit(a)
  def bind[A,B](x: M[A], f: A => M[B]): M[B] =
    r => m.bind(x(r), (a: A) => f(a)(r))
  def ask : M[R] = r => m.unit(r)
  def local[A](f: R => R, a: M[A]) : M[A] = r => a(f(r))
  protected implicit def lift1[A](x: m.M[A]) : M[A] = r => x
  protected implicit def lift2[A,B](x: A => m.M[B]) : A => M[B] =
    a => lift1(x(a))
  protected implicit def lift3[A,B,C](x: (A => m.M[B]) => m.M[C]) : (A => M[B]) => M[C] =
    f => r => x( (a: A) => f(a)(r) )
  protected implicit def lift4[A,B,C,D](x: ((A => m.M[B]) => m.M[C]) => m.M[D]) : ((A => M[B]) => M[C]) => M[D] =
    f => r => x ((a: A => m.M[B]) => f(lift2(a))(r))
}

trait ReaderMonadImpl extends ReaderT {
  val m = IdentityMonad
}

trait StateT extends MonadTransformer with StateMonad {
  type M[A] = S => m.M[(A,S)]
  def unit[A](a: A) : M[A] = (s: S) => m.unit(a,s)
  def bind[A,B](x: M[A], f: A => M[B]) : M[B] = (s: S) => {
    m.bind[(A,S),(B,S)](x(s), { case (a,s2) => f(a)(s2) })
  }
  def getState : M[S] = s => m.unit((s,s))
  def putState(s: S) : M[Unit] = _ => m.unit(( (),s ))
}

trait StateMonadImpl extends StateT {
  val m = IdentityMonad
}

trait ContinuationMonadImpl extends ContinuationMonad {
  type T
  type M[A] = (A => T) => T
  def unit[A](a: A) : M[A] = k => k(a)
  def bind[A,B](m: M[A], f: A => M[B]) : M[B] = k => m( a => f(a)(k) )
  def callCC[A,B](f: (A => M[B]) => M[A]) : M[A] = k => f(a => _ => k(a))(k)
}

trait ReaderContinuationMonadForwarder extends ReaderT with ContinuationMonad {
  val m: ContinuationMonad
  def callCC[A,B](f: (A => M[B]) => M[A]) : M[A] = (m.callCC[A,B] _)(f)
}

trait ReaderContinuationMonadImpl extends ReaderContinuationMonadForwarder {
  type T
  val m = new ContinuationMonadImpl { type T = ReaderContinuationMonadImpl.this.T }
}

trait ReaderStateMonadForwarder extends ReaderT with StateMonad {
  val m: StateMonad { type S = ReaderStateMonadForwarder.this.S }
  override def getState : M[S] = m.getState
  override def putState(s: S) : M[Unit] = m.putState(s)
}

trait ReaderStateMonadImpl extends ReaderStateMonadForwarder {
  val m = new StateMonadImpl { type S = ReaderStateMonadImpl.this.S }
}

/** language "construction kit" */
trait Expressions extends Monad {
  abstract class Value
  abstract class Exp {
    def eval : M[Value]
  }
}

trait Numbers extends Expressions {
  case class NumV(n: Int) extends Value
}

trait Arithmetic extends Numbers {
  case class Num(n: Int) extends Exp {
    def eval = unit(NumV(n))
  }
  implicit def int2exp(n: Int) : Exp = Num(n)
  case class Add(lhs: Exp, rhs: Exp) extends Exp {
    def eval = for {
      l <- lhs.eval
      r <- rhs.eval
    } yield (l,r) match {
      case (NumV(v1),NumV(v2)) => NumV(v1+v2)
      case _ => sys.error("Can only add numbers")
    }
  }
}

trait If0 extends Numbers {
  case class If0(cond: Exp, thenExp: Exp, elseExp: Exp) extends Exp {
    def eval = for {
      c <- cond.eval
      res <- c match {
        case NumV(0) => thenExp.eval
        case NumV(_) => elseExp.eval
        case _ => sys.error("Can only check if number is zero")
      }
    } yield res
  }
}

trait Functions extends Expressions with ReaderMonad {
  type Env = Map[String,Value]
  override type R = Env

  case class ClosureV(f: Fun, env: Env) extends Value
  case class Fun(param: String, body: Exp) extends Exp {
    def eval = for { env <- ask } yield ClosureV(this,env)
  }
  case class App(f: Exp, a: Exp) extends Exp {
    def eval = for {
      fVal <- f.eval
      aVal <- a.eval
      res <- fVal match {
      case ClosureV(Fun(p,b), cEnv) =>
          local( env => cEnv+(p -> aVal), b.eval)
      case _ => sys.error("Can only apply numbers")
    }
    } yield res
  }
  case class Id(x: String) extends Exp {
    def eval = for {
      env <- ask
    } yield env(x)
  }
  implicit def string2exp(s: String) : Exp = Id(s)
  def wth(x: String, xDef: Exp, body: Exp) : Exp = App(Fun(x,body), xDef)
}

trait Boxes extends Expressions with StateMonad {
  override type S = Store
  type Store = Map[Address,Value]
  type Address = Int
  var address = 0
  def nextAddress : Address = {
    address += 1
    address
  }
  case class AddressV(a: Address) extends Value
  case class NewBox(e: Exp) extends Exp {
    def eval = {
      val a = nextAddress
      for {
      eVal <- e.eval
      s <- getState
      _ <- putState(s+(a -> eVal))
    } yield AddressV(a)
    }
  }
  case class SetBox(b: Exp, e: Exp) extends Exp {
    def eval = for {
      bVal <- b.eval
      eVal <- e.eval
      s <- getState
      _ <- putState(bVal match { case AddressV(a) => s+(a -> eVal) })
    } yield eVal
  }
  case class OpenBox(b: Exp) extends Exp {
    def eval = for {
      bVal <- b.eval
      s <- getState
    } yield bVal match { case AddressV(a) => s(a) }
  }
  case class Seq(e1: Exp, e2: Exp) extends Exp {
    def eval = bind(e1.eval, (_: Value) => e2.eval)
  }
}

/** language implementations */
object AE extends Arithmetic with IdentityMonad {
  val test = Add(1,Add(2,3))
  assert(test.eval == NumV(6))
}

object FAE extends Functions with Arithmetic with ReaderMonadImpl {
  val test = App(Fun("x", Add("x",1)), Add(2,3))
}

object BCFAE extends Boxes with Arithmetic with Functions with If0 with ReaderStateMonadImpl {
  val test =
    wth("switch", NewBox(0),
      wth("toggle", Fun("dummy", If0(OpenBox("switch"), SetBox("switch",1), SetBox("switch",0))),
        Add(App("toggle",42), App("toggle",42))))
}

trait LetCC extends Expressions with ContinuationMonad with ReaderMonad {
  override type R = Map[String,Value]
  case class CApp(f: Exp, a: Exp) extends Exp {
    def eval : M[Value] = for {
      fVal <- f.eval
      aVal <- a.eval
      res <- fVal match {
        case ContV(f) => f(aVal)
        case _ => sys.error("Can only apply continuations")
      }
    } yield res
  }
  case class LetCC(param:String, body: Exp) extends Exp {
    def eval : M[Value] = callCC[Value,Value](k => local(env => env+(param -> ContV(k)), body.eval))
  }
  case class ContV(f: Value => M[Value]) extends Value
}

object FAEWithLetCC extends Arithmetic with Functions with If0 with LetCC with ReaderContinuationMonadImpl {
  override type T = Value
  val test = Add(1, LetCC("k", Add(2, CApp("k",3))))
}




