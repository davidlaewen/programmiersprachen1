object InlinedInterpreter {
  sealed abstract class Exp
  case class Num(n: Int) extends Exp
  case class Id(name: String) extends Exp
  case class Add(lhs: Exp, rhs: Exp) extends Exp
  type Value = Int
  type Env = Map[String, Value]
  type R = Env

  type M[X] = R => X
  def unit[A](a: A): M[A] =
    r => a
  def bind[A, B](action1: M[A])(action2: A => M[B]): M[B] =
    r => action2(action1(r))(r)
  def ask: M[R] =
    r => r
  def local[A](f: R => R, p: M[A]): M[A] =
    r => p(f(r))

  //This code adds methods map and flatMap on values of type M[A].
  implicit class monadicSyntax[A](p: M[A]) {
    def flatMap[B](f: A => M[B]) = bind(p)(f)
    def map[B](f: A => B) = flatMap(x => unit(f(x)))
  }

  object OriginalInterpreterVersion {
    def eval(e: Exp): M[Value] = e match {
      case Num(n) =>
        unit(n)
      case Id(x) =>
        for {
          env <- ask
        } yield env(x)
      case Add(l, r) =>
        for {
          lV <- eval(l)
          rV <- eval(r)
        } yield lV + rV
    }
  }

  object InterpreterWithDesugaredForComprehensions {
    def eval(e: Exp): M[Value] = e match {
      case Num(n) =>
        unit(n)
      case Id(x) =>
        ask.map { env =>
          env(x)
        }
      case Add(l, r) =>
      ...
    }
  }

  object InlineMapFlatMap {
    def eval(e: Exp): M[Value] = e match {
      case Num(n) =>
        unit(n)
      case Id(x) =>
        //First, inline map:
        //ask.flatMap(env => unit(env(x)))
        //Then, inline flatMap:
        bind(ask) { env =>
          unit(env(x))
        }
      case Add(l, r) =>
        ???
    }
  }

  object InlineBindUnitAsk {
    def eval(e: Exp): M[Value] = e match {
      case Num(n) =>
        r => n
      case Id(x) =>
        r =>
          ((env: Env) => (r: Env) => env(x))(
            ((r: Env) => r)(r)
          )(r)
      case Add(l, r) =>
        ???
    }
  }

  object FinalSimplifiedInterpreter {
    def eval(e: Exp): Env => Value = e match {
      case Num(n) =>
        r =>
          n
      case Id(x) =>
        env =>
          env(x)
      case Add(l, r) =>
        ???
    }
  }
}