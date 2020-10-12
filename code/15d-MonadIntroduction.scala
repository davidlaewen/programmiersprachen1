import scala.language.implicitConversions
import scala.language.reflectiveCalls

object StandardStyle {
  def f(n: Int) : String = "x"
  def g(x: String) : Boolean = x == "x"
  def h(b: Boolean) : Int = if (b) 27 else sys.error("Error")

  val ex : Int = h(!g(f(27)+"z"))
}

object OptionStyle {
  def f(n: Int) : Option[String] = if (n < 100) Some("x") else None
  def g(x: String) : Option[Boolean] = Some(x == "x")
  def h(b: Boolean) : Option[Int] = if (b) Some(27) else None

  val ex1: Option[Int] = f(27) match {
    case Some(x) => g(x+"z") match {
      case Some(y) => h(!y)
      case None => None
    }
    case None => None
  }

  // abstract over pattern matching structure
  def bind[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
    case Some(x) => f(x)
    case None => None
  }
  def unit[A](x: A) : Option[A] = Some(x)

  val ex2: Option[Int] =
    bind(f(27), (x: String) =>
      bind(g(x+"z"), (y: Boolean) =>
        h(!y)))

  val ex3: Option[Boolean] =
    bind(f(27), (x: String) =>
      bind(g(x+"z"), (y: Boolean) =>
        unit(!y)))

}

/** abstraction over type constructor */
trait Monad[M[_]] { // M is type parameter that expects a function.
  def bind[A,B](m: M[A], f: A => M[B]) : M[B] // Types match those of bind and unit for Options, except
  def unit[A](a: A) : M[A] // that the type constructor is now parameterized.
}

object OptionMonad extends Monad[Option] {
  override def unit[A](a: A) : Option[A] = Some(a)
  override def bind[A,B](m: Option[A], f: A => Option[B]) : Option[B] = m match {
    case Some(y) => f(y)
    case None => None
  }
}

def f(n: Int) : Option[String] = if (n < 100) Some("x") else None
def g(x: String) : Option[Boolean] = Some(x == "x")
def h(b: Boolean) : Option[Int] = if (b) Some(27) else None

def ex1(m: Monad[Option]) : Option[Boolean] =
  m.bind(f(27), (x: String) =>
    m.bind(g(x+"z"), (y: Boolean) =>
      m.unit(!y)))

implicit def monadicSyntax[A, M[_]](m: M[A])(implicit mm: Monad[M]) : Object = new {
  def map[B](f: A => B): Any = mm.bind(m, (x: A) => mm.unit(f(x)))
  def flatMap[B](f: A => M[B]): M[B] = mm.bind(m, f)
}

def ex2(implicit m: Monad[Option]) : Option[Boolean] =
  for { x <- f(27); y <- g(x+"z") } yield !y


