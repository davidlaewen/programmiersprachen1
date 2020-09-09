object guessGame extends App {

    val n = math.floor(math.random() * 100)

    var input = -1
    var inputString = ""

    var won = false

    while (!won) {

      println("Enter a number:")

      inputString = scala.io.StdIn.readLine()
      input = inputString.toInt

      if (input == n) {
        println("You win!")
        won = true
      } else if (input > n)
        println("Too big!")
      else
        println("Too small!")

    }

}

