/** Functional style */
sealed abstract class BTree
case class Node(l: BTree, r: BTree) extends BTree
case class Leaf(n: Int) extends BTree

def treeSum(b: BTree) : Int = b match {
  case Node(l,r) => treeSum(l)+treeSum(r)
  case Leaf(n)   => n
}

val ex1 = Node(Leaf(1),Leaf(2))


/** Visitor style */
case class Visitor[T](node: (T,T) => T, leaf: Int => T)

def foldExp[T](v: Visitor[T], b: BTree) : T = b match {
  case Node(l,r) => v.node(foldExp(v,l), foldExp(v,r))
  case Leaf(n)   => v.leaf(n)
}

val sumVisitor = new Visitor[Int]((l,r) => l+r, n => n)

assert(foldExp(sumVisitor, Node(Leaf(1),Leaf(2))) == 3)

/** Object algebra style */
trait BTreeInt[T] {
  def node(l: T, r: T) : T
  def leaf(n: Int) : T
}

def ex[T](semantics: BTreeInt[T]) : T = {
  import semantics._
  node(leaf(1),leaf(2))
}

object TreeSum extends BTreeInt[Int] {
  def node(l: Int, r: Int) = l+r
  def leaf(n: Int) = n
}

assert(ex(TreeSum) == 3)

// add String leaves
trait BTreeMixed[T] extends BTreeInt[T] {
  def leaf(s: String) : T
}

def ex2[T](semantics: BTreeMixed[T]) : T = {
  import semantics._
  node(leaf(1),leaf("a"))
}

// add new fold operation
object InOrderTraversal extends BTreeMixed[String] {
  def node(l: String, r: String) = l+r
  def leaf(n: Int) = n.toString
  def leaf(s: String) = s
}


