import scala.::

/** Task 1 */

def map(xs: List[Int])(f: Int => Int): List[Int] = xs match {
  case Nil   => Nil
  case x::xs => f(x)::map(xs)(f)
}

def caller1(l: List[Int]) : List[Int] =
  map(l)(x => x+1) ++ map(List(1,2,3))(x => x+2)

object Defunct1 {
  /*
  def add1(x: Int) : Int = x+1
  def add2(x: Int) : Int = x+2

  def caller1(l: List[Int]) : List[Int] =
    map(l)(add1) ++ map(List(1,2,3))(add2)
    */

  sealed abstract class FunctionValueAdd
  case class Add1() extends FunctionValueAdd
  case class Add2() extends FunctionValueAdd

  sealed abstract class FunctionValueMap
  case class Map(xs: List[Int]) extends FunctionValueMap

  def applyAdd(f: FunctionValueAdd, x: Int) : Int = f match {
    case Add1() => x+1
    case Add2() => x+2
  }

  def caller1(l: List[Int]) : List[Int] =
    map(l)(Add1()) ++ map(List(1,2,3))(Add2())

  def map(xs: List[Int])(f: FunctionValueAdd) : List[Int] = xs match {
    case List() => List()
    case x::xs => applyAdd(f,x)::map(xs)(f)
  }


}


/** Task 2 */
def map2(xs: List[Int])(f: Int => Int): List[Int] = xs match {
  case Nil   => Nil
  case x::xs => f(x)::map2(xs)(f)
}

def flatMap(xs: List[Int])(f: Int => List[Int]): List[Int] = xs match {
  case Nil   => Nil
  case x::xs => f(x)++flatMap(xs)(f)
}

def caller2(l: List[Int]) : List[Int] =
  flatMap(List(1,2,3))(x =>
    map2(List(x + 1))(y =>
      x+y))

object Defunct2 {
  // def f(x: Int) : List[Int] = map2(List(x+1))(g(x))
  // def g(x: Int)(y: Int) : Int = x+y

  sealed abstract class FunctionValueIntInt
  case class G(x: Int) extends FunctionValueIntInt

  sealed abstract class FunctionValueIntListInt
  case class F() extends FunctionValueIntListInt

  def applyIntInt(f: FunctionValueIntInt, y: Int) : Int = f match {
    case G(x) => x+y
  }

  def applyIntListInt(f: FunctionValueIntListInt, x: Int) : List[Int] = f match {
    case F() => map2(List(x+1))(G(x))
  }

  def map2(xs: List[Int])(f: FunctionValueIntInt) : List[Int] = xs match {
    case Nil   => Nil
    case x::xs => applyIntInt(f,x)::map2(xs)(f)
  }

  def flatMap(xs: List[Int])(f: FunctionValueIntListInt) : List[Int] = xs match {
    case Nil   => Nil
    case x::xs => applyIntListInt(f,x) ++ flatMap(xs)(f)
  }

  def caller2(l: List[Int]) : List[Int] =
    flatMap(List(1,2,3))(F())
}


/** Task 3 */
def flatMap2(xs: List[Int])(f: Int => List[Int]): List[Int] = xs match {
  case Nil   => Nil
  case x::xs => f(x)++flatMap2(xs)(f)
}

def map3(xs: List[Int])(f: Int => Int): List[Int] =
  flatMap2(xs)( x => List(f(x)) )

def caller3(l: List[Int]): List[Int] =
  flatMap2(List(1,2,3))(x =>
    map3(List(x*3))(y =>
      x + y + 42))

object Defunct3 {
  sealed abstract class FunctionValueIntInt
  case class PlusOp(x: Int) extends FunctionValueIntInt

  sealed abstract class FunctionValueIntListInt
  case class MapOp() extends FunctionValueIntListInt
  case class ListOp(f: FunctionValueIntInt) extends FunctionValueIntListInt

  def applyIntInt(f: FunctionValueIntInt, y: Int) : Int = f match {
    case PlusOp(x) => x+y+42
  }

  def applyIntListInt(f: FunctionValueIntListInt, x: Int) : List[Int] = f match {
    case MapOp() => map3(List(x*3))(PlusOp(x))
    case ListOp(f) => List(applyIntInt(f,x))
  }

  def flatMap2(xs: List[Int])(f: FunctionValueIntListInt) : List[Int] = xs match {
    case Nil => Nil
    case x::xs => applyIntListInt(f,x)++flatMap2(xs)(f)
  }

  def map3(xs: List[Int])(f: FunctionValueIntInt) : List[Int] =
    flatMap2(xs)(ListOp(f))

  def caller3(l: List[Int]) : List[Int] =
    flatMap2(List(1,2,3))(MapOp())
}









