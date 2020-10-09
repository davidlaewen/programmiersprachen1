import scala.collection.mutable
import scala.io.StdIn.readLine

/** Original program */
def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def progSimple(): Unit = {
  println(inputNumber("Enter first number:") + inputNumber("Enter second number:"))
}

/** Modeling through command line input and output */
// terminate the program after every output to model statelessness
def webDisplay(s: String) : Nothing = {
  println(s)
  sys.error("Program terminated")
}

def webRead(prompt: String, continue: String) : Nothing = {
  println(prompt)
  println("Send input to "+continue)
  sys.error("Program terminated")
}

def progA = webRead("Enter first number:", "progB")
def progB(n: Int) = webRead("First number was "+n+"\nSecond number:", "progC")
def progC(n1: Int, n2: Int) = webDisplay("Sum of "+n1+" and "+n2+" is "+(n1+n2))

/** Continuations */
val cont1 = (n: Int) => println(n + inputNumber("Enter second number"))
// val cont2 = (m: Int) => println(n+m)

val continuations = new mutable.HashMap[String, Int=>Nothing]
var nextIndex : Int = 0
def getNextId : String = {
  nextIndex += 1
  "c"+nextIndex
}

def webReadCont(prompt: String, k: Int => Nothing) : Nothing = {
  val id = getNextId
  continuations += (id -> k)
  println(prompt)
  println("To continue, invoke continuation "+id)
  sys.error("Program terminated")
}

def continue(kId: String, result: Int): Nothing = continuations(kId)(result)

def webProg =
  webReadCont("First number:", (n: Int) =>
    webReadCont("Second number:", (m: Int) =>
      webDisplay("Sum of "+n+" and "+m+" is "+(n+m))))

