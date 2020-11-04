/** Function for testing */
def webDisplay(x: Any) : Nothing = {
  println(x.toString)
  sys.error("Program terminated")
}


/** Tasks */

object Task1 {
  val x: Int = 42
  def x_k (k: Int => Nothing) : Nothing = k(42)

  def test() : Nothing = x_k(webDisplay(_))
}


object Task2 {
  def even(n: Int) : Boolean = n match {
    case 0 => true
    case n => odd(n-1)
  }
  def odd(n: Int) : Boolean = n match {
    case 0 => false
    case n => even(n-1)
  }

  def even_k(n: Int, k: Boolean => Nothing) : Nothing = n match {
    case 0 => k(true)
    case n => odd_k(n-1,k)
  }
  def odd_k(n: Int, k: Boolean => Nothing) : Nothing = n match {
    case 0 => k(false)
    case n => even_k(n-1,k)
  }

  def test1() : Nothing = even_k(2, webDisplay(_))
  def test2() : Nothing = even_k(7, webDisplay(_))
  def test3() : Nothing =  odd_k(1, webDisplay(_))
  def test4() : Nothing =  odd_k(4, webDisplay(_))
}


object Task3 {
  def f(n: Int) : Int = n+1
  def f_k(n: Int, k: Int => Nothing) : Nothing = k(n+1)

  val res: Int = f(f(40))
  def res_k(k: Int => Nothing) : Nothing = f_k(40, n => f_k(n,k))

  def test() : Nothing = res_k(webDisplay(_))
}


object Task4 {
  def map(l: List[Int], f: Int => Int) : List[Int] = l match {
    case List() => List()
    case x::xs => f(x)::map(xs,f)
  }
  val res: List[Int] = map(List(1,2,3), x => x+1)

  def map_k(l: List[Int],
            f: (Int, Int => Nothing) => Nothing,
            k: List[Int] => Nothing) : Nothing = l match {
    case List() => k(List())
    case x::xs => f(x, y => map_k(xs, f, (ys: List[Int]) => k(y::ys)))
  }
  def res_k(k: List[Int] => Nothing) : Nothing =
    map_k(List(1,2,3), (x: Int, k: Int => Nothing) => k(x+1), k)

  def test() : Nothing = res_k(webDisplay(_))
}


object Task5 {
  def g(n: Int) : Int = n+1
  def h(n: Int) : Int = n*n
  val res: Int = g(1) + h(2)

  def g_k(n: Int, k: Int => Nothing) : Nothing = k(n+1)
  def h_k(n: Int, k: Int => Nothing) : Nothing = k(n*n)
  def res_k(k: Int => Nothing) : Nothing = g_k(1, n => h_k(2, m => k(n+m)))

  def test() : Nothing = res_k(webDisplay(_))
}


object Task6 {
  def sum(n: Int) : Int = n match {
    case 0 => 0
    case n => n + sum(n-1)
  }
  val res: Int = sum(10)

  def sum_k(n: Int, k: Int => Nothing) : Nothing = n match {
    case 0 => k(0)
    case n => sum_k(n-1, m => k(n+m))
  }
  def res_k(k: Int => Nothing) : Nothing = sum_k(10, k)

  def test() : Nothing = res_k(webDisplay(_))
}



