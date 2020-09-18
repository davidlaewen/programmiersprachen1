```scala
val faeProgram1 =
  wth("fun", Fun("x", Add("x", 0)),
    Add(App("fun", 0), App("fun", 0)))
/** evaluates to 0.
 * With fun = (x => x+0):
 *    fun(0) + fun(0)
 */

val faeProgram2 =
  wth("fun", Fun("x", Add("x", 0)),
    wth("x", App("fun", 0),
      Add("x", "x")))
/** evaluates to 0.
 * With fun = (x => x+0):
 *    With x = fun(0)
 *       x + x
 */
```

In the following, two programs are equivalent if they evaluate to the same numerical value.

Answer the following questions:

Are they equivalent?
> Both programs are equivalent since they evaluate to the same numerical value `0`.

Do they stay equivalent if we replace the body of function "fun" by another arbitrary FAE expression?
> FAE is purely functional and therefore exhibits _referential transparency_, meaning replacing function calls with their respective results leads to an equivalent program. In the second example, the result of of calling `fun` on `0` is simply bound to `x`, `x` is then used in place of the function calls in the final `Add` expression. Therefore the function calls were simply replaced with their result and the programs are equivalent.


Now, consider the following two BCFAE programs:

```scala
val bcfaeProgram1 =
  wth("counter", NewBox(0),
    wth("fun", Fun("x", Add("x", 0)),
      Add(App("fun", 0), App("fun", 0))))
val bcfaeProgram2 =
   wth("counter", NewBox(0),
     wth("fun", Fun("x", Add("x", 0)),
       wth("x", App("fun", 0),
         Add("x", "x"))))
```

Answer the following questions:

- Are they equivalent?
> Both programs evaluate to `0`, hence they are equivalent.

- Do they stay equivalent if we replace the body of function "fun" by
another arbitrary FAE expression (that is, a BCFAE expression that only uses
the syntactic constructs available in FAE)?
> Yes, since an FAE expression has no side effects (mutation of box values). Therefore binding the result of the function call to `x` and then using `x` in place of the function call will not change the evaluation result.

- Do they stay equivalent if we replace the body of function "fun" by
another arbitrary BCFAE expression?
> In this case, calling `fun` could have side effects such as mutating the value inside the box bound to `counter`. For this reason, calling the function once and binding the result could lead to a different outcome than calling the function twice (e.g. if the value in `counter` is incremented with each call of `fun` and then returned, the result would differ when performing two function calls instead of one. 


Hints:

- Try this out with the appropriate interpreter from the lecture notes.
- This question is about the relation between mutation and purity.
If we call the same function twice with the same argument, are we going to
always get the same result, or not?

