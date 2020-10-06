import scala.language.implicitConversions

/** Task */
trait BinaryIntTree[T] {
  implicit def leaf(n: Int) : T
  def node(l: T, r: T) : T
}

/** Bonus */
object TreeSum extends BinaryIntTree[Int] {
  def leaf(n: Int) : Int = n
  def node(l: Int, r: Int) : Int = l+r
}

object CountNodes extends BinaryIntTree[Int] {
  def leaf(n: Int) : Int = 1
  def node(l: Int, r: Int) : Int = l+r
}

trait BinaryMixedTree[T] extends BinaryIntTree[T] {
  implicit def leaf(s: String) : T
}

object InOrderTraversal extends BinaryMixedTree[String] {
  def leaf(n: Int) : String = n.toString
  def leaf(s: String) : String = s
  def node(l: String, r: String) : String = l+","+r
}

/** examples */
def tree1[T](semantics: BinaryIntTree[T]) : T = {
  import semantics._
  node(1,2)
}
assert(tree1(TreeSum) == 3)
assert(tree1(CountNodes) == 2)

def tree2[T](semantics: BinaryIntTree[T]) : T = {
  import semantics._
  node( node(1,2), node(3, node(4,5)) )
}
assert(tree2(TreeSum) == 15)
assert(tree2(CountNodes) == 5)
assert(tree2(InOrderTraversal) == "1,2,3,4,5")

def tree3[T](semantics: BinaryMixedTree[T]) : T = {
  import semantics._
  node( node("t","r"), node("e","e") )
}
assert(tree3(InOrderTraversal) == "t,r,e,e")

