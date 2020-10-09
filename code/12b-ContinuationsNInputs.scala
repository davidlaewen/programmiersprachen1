import scala.collection.mutable
import scala.io.StdIn.readLine

/** Normal style */
def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def addAllCosts(items: List[String]) : Int = items match {
  case List() => 0
  case first::rest => inputNumber("Cost of "+first+":") + addAllCosts(rest)
}

def map[S,T](c: List[S], f: S => T) : List[T] = c match {
  case List() => List()
  case first::rest => f(first)::map(rest,f)
}

def addAllCostsMap(items: List[String]) : Int = {
  map(items, (s: String) => inputNumber("Cost of " + s + ":")).sum
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
    case first::rest =>
      webReadCont("Cost of "+first+":",
        (n: Int) => addAllCostsCont(rest, (m: Int) => k(m+n)))
  }
}

def testWeb() : Unit = addAllCostsCont(testList, m => webDisplay("Total cost: "+m))

/** Web transformation of 'map', 'addAllCostsCont' implementation with 'map' */
def mapCont[S,T](c: List[S],
                 f: (S, T => Nothing) => Nothing,
                 k: List[T] => Nothing) : Nothing = c match {
  case List() => k(List())
  case first :: rest => f(first, t => mapCont(rest, f, (tr: List[T]) => k(t::tr)))
}

def addAllCostsCont2(itemList: List[String], k: Int => Nothing) : Int =
  mapCont(itemList,
    (x: String, k2: Int => Nothing) => webReadCont("Cost of "+x+":", k2),
    (l: List[Int]) => k(l.sum)
  )

def testWeb2() : Unit = addAllCostsCont2(testList, m => webDisplay("Total cost: "+m))


