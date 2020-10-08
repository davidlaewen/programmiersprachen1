import scala.collection.mutable
import scala.io.StdIn.readLine

/** Normal style */
def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def addAllCosts(items: List[String]): Int = items match {
  case List() => 0
  case first :: rest => inputNumber("Cost of "+first+":") + addAllCosts(rest)
}

val testList = List("Banana", "Apple", "Orange")
def test() : Unit = println("Total cost: " + addAllCosts(testList))


/** Continuation style */
def webDisplay(s: String) : Nothing = {
  println(s)
  sys.error("Program terminated")
}

val continuations = new mutable.HashMap[String, Int=>Nothing]
var nextIndex : Int = 0
def getNextId : String = {
  nextIndex += 1
  "c"+nextIndex
}

def continue(kId: String, result: Int): Nothing = continuations(kId)(result)

def webReadCont(prompt: String, k: Int => Nothing) : Nothing = {
  val id = getNextId
  continuations += (id -> k)
  println(prompt)
  println("To continue, invoke continuation "+id)
  sys.error("Program terminated")
}

def addAllCostsCont(itemList: List[String], k: Int => Nothing) : Nothing = {
  itemList match {
    case List() => k(0)
    case first :: rest =>
      webReadCont("Cost of "+first+":",
        (n: Int) => addAllCostsCont(rest, (m: Int) => k(m+n)))
  }
}

def testWeb() : Unit = addAllCostsCont(testList, m => webDisplay("Total cost: "+m))


