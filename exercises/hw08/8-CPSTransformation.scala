/** Function for testing */
def webDisplay(x: Any) : Nothing = {
  println(x.toString)
  sys.error("Program terminated")
}


/** Tasks */
object Task1 {
  def f(n: Int) : Int = n+1
  val res: Int = f(f(3) + f(4))

  def f_k(n: Int, k: Int => Nothing) : Nothing = k(n+1)
  def res_k(k: Int => Nothing) : Nothing = f_k(3, n => f_k(4, m => f_k(n+m,k)))

  def test() : Nothing = res_k(webDisplay(_)) // ((3+1)+(4+1))+1 = 10
}

object Task2 {
  def all(f: Int => Boolean, l: List[Int]) : Boolean = l match {
    case List() => true
    case x::xs => f(x) && all(f,xs)
  }
  val even: Int => Boolean = (n: Int) => n % 2 == 0
  assert(all(even, List(2,4,6,8,10)))
  assert(!all(even, List(2,4,7,8,10)))

  def all_k(f: (Int, Boolean => Nothing) => Nothing,
            l: List[Int],
            k: Boolean => Nothing) : Nothing = l match {
    case List() => k(true)
    case x::xs => f(x, b1 => all_k(f, xs, b2 => k(b1&&b2)))
  }
  def even_k(n: Int, k: Boolean => Nothing) : Nothing = k(n%2 == 0)

  def test1() : Nothing = all_k(even_k, List(2,4,6,8,10), webDisplay(_))
  def test2() : Nothing = all_k(even_k, List(2,4,7,8,10), webDisplay(_))
}

