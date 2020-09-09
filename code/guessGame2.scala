object GuessGame extends App {

  val n = math.floor(math.random() * 100)

  println("Welcome to Guess Game!")

  var guessing = true
  var input = -1
  var inputString = ""

  while(guessing) {

    println("Guess a number and press 'Enter'!")

    inputString = scala.io.StdIn.readLine()
    input = inputString.toInt

    if (input == n) {
      println("You win!")
      guessing = false
    }
    else if (input > n) println("Too big!")
    else println("Too small!")

  }

}