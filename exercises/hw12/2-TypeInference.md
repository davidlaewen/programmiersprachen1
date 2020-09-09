```scala
val e1 = Fun("x", "x")
forall alpha . alpha -> alpha
Function<alpha, alpha>

val e2 = Fun("x", Add("x", 1))
Int -> Int
Function<Int, Int>

//This term is in an open context.
val e3: Exp = Add("x", 1)


val e3Ctx = Map("x" -> freshTypeVar)
Map<alpha, ???>

val apply: Exp = Fun("f", Fun("x", App("f", "x")))
???

val compose: Exp = Fun("f", Fun("g", Fun("x", App("g", App("f", "x")))))

```
