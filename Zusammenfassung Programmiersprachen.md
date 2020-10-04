---
title: Zusammenfassung Programmiersprachen
description: Programmiersprachen 1, SoSe 2020, Klauso3
langs: de

---

# Inhaltsverzeichnis

[TOC]

# Vorlesungsinhalte
- gutes Verständnis von Programmiersprachen (allgemein, über _Trends_ hinweg) und deren Qualitäten, Vor- und Nachteile
- Fähigkeit, Programmiersprachen in Features zu zerlegen und diese einzeln zu verstehen und zu analysieren
- Implementieren von Programmiersprachen(-features) durch Interpreter in Scala
- Auseinandersetzung mit wenigen Compiler-bezogenen Themen
- Sprachen und deren Features, Zweck/Nutzen dieser Features, mögliche Implementationen und Vorzüge/Probleme dieser

# Scala-Grundlagen
[Scala](https://scala-lang.org/) ist statisch getypt, funktional, sowie objekt-orientiert. Scala-Programme können zu Java-Bytecode kompiliert und in einer JVM ausgeführt werden. Auswertung ist _eager_ (_call by value_).
- **Konstanten** mit `val`, mutierbare **Variablen** mit `var`. Typ muss nicht deklariert werden, also bspw. `var n = 1` oder `var s = "abc"`. Typ kann aber auch explizit deklariert werden, also bspw. `var n: Int = 1` oder `var s: String = "abc"`.
- **Funktionen** haben die Form `def f(<arg1>: <Type1>, ...) = <body>`, Aufruf bspw. durch `f(1)`. Rückgabetyp kann optional angegeben werden: `def f(<arg1>: <Type1>, ...): <ReturnType> = <body>`
```scala
def sum(a: Int, b: Int): Int = a + b
val x = sum(5, 11)

def concat(a: String, b: String): String = a + b
val y = concat("Manfred", concat(" ", "Opel"))
```

## Datentypen
- `Int`, `String`, `Boolean`, `Double`, etc.
- `Unit` entspricht Rückgabetyp `void` in Java (kein Rückgabewert, sondern "Seiteneffekt", bspw. `print`-Funktion)
- **Map:** Abbildung einer Menge von Werten auf eine andere Menge von Werten. 
```scala
var map = Map(1 -> "a", 2 -> "b")
assert(map(1) == "a")
```
- **Tupel:** `(<first>, <second>, ...)`, erlauben Gruppierung heterogener Daten. Können zwischen 2 und 22 Werte beliebigen Typs enthalten.
```scala
val t = (1, "abc", new Person("Mani"))
val firstEntry = t._1
val (num, string, person) = t
assert(firstEntry == num)
```
- **Listen:** `List(<first>, <second>, ...)`, Zugriff mit `<list>(<index>)`, nicht mutierbar
```scala
val nums = List.range(0, 10)
val nums = (1 to 10 by 2).toList
val letters = ('a' to 'f' by 2).toList

letters.foreach(println)
nums.filter(_ > 3).foreach(println)
val doubleNums = nums.map(_ * 2) 
val bools = nums.map(_ < 5)
val squares = nums.map(math.pow(_,2).toInt)
val sum = foldLeft(0)(_ + _)
val prod = foldLeft(1)(_ + _)
```
- **Arrays:** `Array(<first>, <second>, ...)`, Zugriff auch mit `<array>(<index>)`, mutierbar mit `<array>(<index>) = <new value>`

- **Mengen:** `Set(<elem1>, <elem2>, ...)`, mutierbar mit `+` und `-` um einzelne Werte hinzuzufügen bzw. zu entfernen, `++` und `--` für Vereinigung bzw. Schnitt mit anderer Menge oder Liste.

- **Either:** Repräsentiert einen Wert eines von zwei möglichen Typen. Jede Instanz von `Either` ist eine Instanz von `Right` oder von `Left`.

## Objektorientierung
- **Klassen:**
```scala
class Person(var firstName: String, var lastName: String) {
    def sayHello() = {
        print(s"Hello, $firstName $lastName")
    }
}

val mani = new Person("Manfred", "Opel")

mani.sayHello() // prints "Hello, Manfred Opel"
println(mani.firstName) // prints "Manfred"
mani.firstName = "Mampfred" // fields can be accessed without get and set methods
mani.lastName = "Mompel"
mani.sayHello() // now prints "Hello, Mampfred Mompel"
```
- Werden die Felder der Klasse mit `var` definiert, so sind sie mutierbar.
- **Abstrakte Klassen** mit Keyword `abstract`
- **Traits:** Können nicht instanziiert werden, sondern sind Bausteine zur Konstruktion von Klassen. Lassen sich einer Klasse mit `with`/`extends` anfügen. Ist ein Trait _sealed_ (`sealed trait ...`), so müssen alle erbenden Klassen in der gleichen Datei definiert sein. In diesem Fall kann bei Pattern Matching erkannt werden, ob alle Fälle (d.h. Case Classes) abgedeckt sind.
```scala
trait Speaker {
    def sayCatchPhrase(): Unit // no function body, abstract
}

trait Sleeper {
    def sleep(): Unit = println("I'm sleeping")
    def wakeUp(): Unit = println("I'm awake")
}

class Person(name: String, catchPhrase: String) extends Speaker with Sleeper {
    def sayCatchPhrase(): Unit = println(catchPhrase)
}
```
Überladen von Methoden in Klassen mit Keyword `override` möglich
- **Objekte:** Können mit Keyword `object` instanziiert werden.
- Erweiterung von Klassen/Traits oder Implementation abstrakter Klassen mit `extends`
- Case Classes: Hilfreich bei der Verwendung von Klassen als Datencontainer. Erzeugung von Instanzen ist ohne `new` möglich, zudem gibt es eine Default-Implementation zum Vergleichen oder Hashen von Instanzen der Klasse. Mit Case Classes ist Pattern Matching möglich:
```scala
trait UniPerson
case class Student(val id: Int) extends UniPerson
case class Professor(val subject: String) extends UniPerson

def display(p: UniPerson) : String =
    p match {
        case Student(id) => s"Student number $id"
        case Professor(subject) => s"Professor of $subject"
    }
```

Alternativ:
```scala
abstract class UniPerson {
    def display : String
}
class Student(val id: Int) extends UniPerson {
    def display = s"Student number $id"
}
class Professor(val subject: String) extends UniPerson {
    def display = s"Professor of $subject"
}
```
Die erste Variante (Pattern-Match-Dekomposition) erlaubt das Hinzufügen weiterer Funktionen, die auf _Uni-Personen_ operieren, ohne den bestehenden Code zu modifizieren.

Die zweite Variante (objektorientierte Dekomposition) erlaubt das Hinzufügen weiterer _Uni-Personen_, ohne dass der bestehende Code verändert werden muss. 

Dieser Konflikt wird _Expression Problem_ genannt.

## Kontrollstrukturen
- `if`-`else`-Statements:
```scala
if (<check>) <statement>

if (<check1>) {
    <statement1>
} else if (<check2>) {
    <statement2>
} else {
    <statement3>
}

val x = if (1 == 1) "a" else "b" // can be used as ternary operator
```

- `for`-Schleifen: 
```scala
for (elem <- list) println(elem)

for (i <- 0 to 10 by 2) println(i)

val evenNums = for {
  i <- 0 to 10
  if i % 2 == 0
} yield i
// evenNums: Vector(0,2,4,6,8,10)
```

## Pattern Matching
```scala
def pm(x: Any) = x match {
  case 1 => "x is 1"
  case true => "x is true"
  case s: String => s"x is string $s"
  case (a,b,c) => s"x is tuple of $a, $b and $c"
  case _ => "x is something else"
}
```

## REPL
- REPL starten mit Befehl `scala`
- `.scala`-Datei in REPL laden mit `:load <filename>.scala`
- Ergebnisse von Auswertung werden automatisch an Variablennamen gebunden
- Bisherige Definitionen können mit `:reset` gelöscht werden
- REPL verlassen mit `:q`


## Implizite Konvertierung
Scala bietet die Möglichkeit, bestimmte Typkonvertierungsfunktionen automatisch zu nutzen, wenn so der erwartete Typ erfüllt werden kann. Dadurch können wir Ausdrücke geschickter notieren.

Hierzu muss die `implicitConversions`-Bibliothek importiert werden:
```scala
import scala.language.implicitConversions

implicit def num2exp(n: Int) : Exp = Num(n)

val test = Add(1,2) // is converted to Add(Num(1), Num(2))
```
Die Funktion `num2exp` wird durch das Keyword `implicit` automatisch auf Werte vom Typ `Int` aufgerufen, wenn an deren Stelle ein Wert vom Typ `Exp` erwartet wird.

## Typ-Alias
Mit dem Keyword `type` können neue Typen definiert werden:
```scala
type IntStringMap = Map[Int, String]
```

## Lambda-Ausdrücke und Currying
Es können in Scala Funktionen als Werte (_Lambda-Ausdrücke_) definiert werden. Diese haben dann einen Typ der Form `<Type> => ...`:
```scala
val succ : Int => Int = n => n+1
```
Funktionen bzw. Lambda-Ausdrücke können dadurch Rückgabewert von Funktionen sein (_Higher Order_), wodurch auch _Currying_ möglich ist:
```scala
def curryAdd(n: Int) : (Int => Int) = (x => x+n)
assert(curryAdd(3)(4) == 7)
val curried : Int => Int => Int => Int = a => b => c => a*b*c
assert(curried(1)(2)(3) == 6)
```



# Erster Interpreter (AE)
```scala
sealed trait Exp
case class Num(n: Int) extends Exp // inherits properties of Scala Int type!
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
}

// examples:
var onePlusTwo = Add(Num(1), Num(2))
assert(eval(onePlusTwo) == 3)
var twoTimesFour = Mul(Num(2), Add(Num(1), Num(3)))
assert(eval(twoTimesFour) == 8)
var threeTimesFourPlusFour = Add(Mul(Num(3),Num(4)), Num(4))
assert(eval(threeTimesFourPlusFour) == 16)
```
Bei der Implementation eines Interpreters ist ein vollständiges Verständnis der Metasprache (hier Scala) notwendig, um die Eigenschaften der Implementation vollständig zu kennen. Der `Int`-Datentyp hat bspw. gewisse Einschränkungen, die nun auch in der implementierten Sprache existieren.

# Syntaktischer Zucker und Desugaring
In vielen Programmiersprachen gibt es prägnantere Syntax, die gleichbedeutend mit einer ausführlicheren Syntax ist (_syntaktischer Zucker_). Das erspart Schreibaufwand beim Programmieren und verbessert die Lesbarkeit von Programmen, ist aber bei der Implementierung der Sprache lästig, da man gleichbedeutende Syntax mehrfach implementieren muss. 
Der syntaktische Zucker erweitert den Funktionsumfang der Sprache nicht und jeder Ausdruck kann mit der gleichen Bedeutung ohne syntaktischen Zucker formuliert werden. Deshalb werden Sprachen typischerweise in die _Kernsprache_ und die _erweiterte Sprache_ aufgeteilt, so dass Ausdrücke vor dem Interpretieren zuerst in eine Form ohne die erweiterte Sprache gebracht werden können (_Desugaring_). So muss der Interpreter nur für die Kernsprache implementiert werden. 

## Interpreter mit Desugaring
```scala
// core language
sealed trait CExp
case class CNum(n: Int) extends CExp 
case class CAdd(l: CExp, r: CExp) extends CExp
case class CMul(l: CExp, r: CExp) extends CExp

// extended language
sealed trait SExp
case class SNum(n: Int) extends SExp
case class SAdd(l: SExp, r: SExp) extends SExp
case class SMul(l: SExp, r: SExp) extends SExp
case class SSub(l: SExp, r: SExp) extends SExp

def desugar(s: SExp) : CExp = s match { 
  case SNum(n) => CNum(n)
  case SAdd(l,r) => CAdd(desugar(l), desugar(r))
  case SMul(l,r) => CMul(desugar(l), desugar(r)) 
  case SSub(l,r) => CAdd(desugar(l), CMul(CNum(-1), desugar(r)))
}

def eval(c: CExp) : Int = c match {
  case CNum(n) => n
  case CAdd(l,r) => eval(l) + eval(r)
  case CMul(l,r) => eval(l) * eval(r)
}

// examples:
var twoMinusOne = SSub(SNum(2), SNum(1))
assert(eval(desugar(twoMinusOne)) == 1)
var fivePlusFour = SAdd(SNum(5), SNum(4))
assert(eval(desugar(fivePlusFour)) == 9)
```

Alternativ kann für unsere Zwecke syntaktischer Zucker in einer Funktion definiert werden, so dass kein Desugaring notwendig ist.
```scala
def neg(e: Exp) = Mul(Num(-1), e)
def sub(l: Exp, r: Exp) = Add(l, neg(r))
```
Syntaktischer Zucker kann (wie in `sub`) auch auf anderem syntaktischen Zucker aufbauen.



# Abstraktion durch Visitor
Eine alternative Möglichkeit, den ersten Interpreter zu definieren, ist durch einen sogenannten _Visitor_. Dabei handelt es sich um eine Instanz einer Klasse mit Typparameter `T`, die aus Funktionen mit den Typen `Int => T` und `(T,T) => T` besteht.

```scala
case class Visitor[T](num: Int => T, add: (T,T) => T)

def foldExp[T](v: Visitor[T], e: Exp) : T = e match {
  case Num(n) => v.num(n)
  case Add(l,r) => v.add(foldExp(v,l), foldExp(v,r))
}
```

Der wesentliche Unterschied zwischen einem Interpreter und einem Visitor ist, dass der Visitor selbst nicht rekursiv ist. Stattdessen wird das grundlegende Rekursionsmuster in einer Funktion abstrahiert (als Faltung, hier in `foldExp`). Dadurch können beliebige _kompositionale_ Definitionen auf der Datenstruktur (hier `Exp`) definiert werden, ohne dass weitere Funktionen notwendig sind. 

:::info
**Kompositionalität** heißt, dass sich die Bedeutung eines zusammengesetzten Ausdrucks aus der Bedeutung seiner Bestandteile ergibt. Bei einer rekursiven Struktur muss also die Bedeutungsfunktion strukturell rekursiv sein.
:::

Auch in dieser Implementation ist es möglich, eine Core-Sprache und eine erweiterte Sprache zu definieren, in dem man etwa eine zweite Klasse definiert, die `Visitor` erweitert und um zusätzliche Funktionen (bspw. `mul: (T,T) => T`) ergänzt.

Es können auch wieder Identifier mithilfe einer _Environment_ der Sprache hinzugefügt werden, der `eval`-Visitor muss dazu mithilfe von Currying verfasst werden. Der Typ des Visitors ist dann `Env => Int`, es wird erst ein Ausdruck und anschließend eine Umgebung überreicht, bevor das Ergebnis ausgegeben wird, dadurch lässt sich die Funktion trotz des zusätzlichen Parameters mit der Visitor-Klasse verfassen.


# Identifier mit Umgebung (AEId)
Um Identifier in den Ausdrücken verwenden zu können, ist eine zusätzliche Datenstruktur notwendig, nämlich eine Umgebung (_environment_), in der die Paare aus Identifiern und Werten gespeichert und ausgelesen werden.
Wir verwenden für die Identifier den Datentyp `Symbol`, für die Umgebung definieren wir das Typ-Alias `Env`, dass eine `Map` von `Symbol` nach `Int` bezeichnet.
```scala
import scala.language.implicitConversions

// ...
case class Id(x: Symbol) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(Symbol(s))

// type definition, 'Env' is alias for type on right hand side
type Env = Map[Symbol, Int]

def eval(e: Exp, env: Env) : Int = e match {
  case Num(n) => n
  // pass environment on during recursion
  case Add(l,r) => eval(l,env) + eval(r,env)
  case Mul(l,r) => eval(l,env) * eval(r,env)
  // look up identifier in map, return associated value 
  case Id(x) => env(x)
}

// examples
val env = Map('x -> 2, 'y -> 4)
val a = Add(Mul("x",5), Mul("y",7))
assert(eval(a, env) == 38)
val b = Mul(Mul("x", "x"), Add("x", "x"))
assert(eval(b, env) == 16)
```

# Identifier mit Bindings (WAE)
Bei der Implementation von Identifiern mit einer Environment müssen die Identifier außerhalb des Ausdrucks in der _Map_ definiert werden und Identifier können nicht umdefiniert werden. 

Besser wäre eine Implementation, bei der die Bindungen innerhalb des Ausdrucks selbst definiert und umdefiniert werden können. Dazu ist ein neues Sprachonstrukt, `With`, notwendig. Ein `With`-Ausdruck besteht aus einem Identifier, einem Ausdruck und einem Rumpf, in dem der Identifier an den Ausdruck gebunden ist.
```scala
case class With(x: Symbol, xdef: Exp, body: Exp) extends Exp
```

Die Definition soll nur im Rumpf gelten, nicht außerhalb (_lexikalisches Scoping_).

`Add(5, With(x, 7, Add(x, 3)))` soll also bspw. zu `15` auswerten.

Dazu soll die Definition von `x` (hier `7`) ausgewertet und für alle Vorkommen von `x` im Rumpf eingesetzt werden (_Substitution_). Hierfür definieren wir eine neue Funktion `subst` mit dem Typ `(Exp, Symbol, Num) => Exp`. Es müssen zwei verschiedene Vorkommen von Identifiern unterschieden werden: _Bindende_ Vorkommen (Definitionen in `With`-Ausdrücken) und _gebundene_ Vorkommen (Verwendung an allen anderen Stellen). Tritt ein Identifier auf, ohne dass es "weiter außen" im Gesamtausdruck ein bindendes Vorkommen gibt, so handelt es sich um ein _freies_ Vorkommen. 

```scala
With x = 7: // binding occurence
   x + y // x bound, y free
```

:::info
Das **Scope** eines bindenden Vorkommens des Identifiers `x` ist die Region des Programmtextes in dem sich Vorkommen von `x` auf dieses bindende Vorkommen beziehen.
:::

Bei der Implementation muss festgelegt werden, in welchem Teil eines Ausdrucks ein bindendes Vorkommen gelten soll.
- Beispiel 1: Das gebundene Vorkommen von `x` soll sich auf die Definition in der ersten Zeile beziehen, die zweite Definition soll keine Wirkung "nach außen" haben.
```scala
With x = 5:
   x + (With x = 3: 10)
```
- Beispiel 2: Das erste Vorkommen soll hier (intuitiv, vgl. Scheme/Racket) den Wert 5 besitzen, dass zweite Vorkommen jedoch den Wert 3. Es soll also immer das nächste bzw. nächstinnerste bindende Vorkommen gelten.
```scala
With x = 5:
   x + (With x = 3: x)
```
Würde das Scope des ersten bindenden Vorkommens den gesamten Ausdruck umfassen, so wäre es nicht möglich, Identifier umzubinden, außerdem sind Ausdrücke so weniger verständlich, da der innere Ausdruck (oben in Klammern) sonst je nach Kontext zu einem anderen Ergebnis auswerten würde

Es ergibt sich die folgende Implementation für die Erweiterung um `With` und Substitution:
```scala
case class With(x: Symbol, xdef: Exp, body: Exp) extends Exp

def subst(body: Exp, i: Symbol, v: Num) : Exp = body match {
  case Num(n) => body
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else body
  case With(x,xdef,body) =>  // do not substitute in body if x is redefined
    With(x, subst(xdef,i,v), if (x == i) body else subst(body,i,v))
}

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x.name)
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
  case With(x,xdef,body) => eval(subst(body,x,Num(eval(xdef))))
}

val a = Add(5, With('x, 7, Add("x", 3)))
assert(eval(a) == 15)
val b = With('x, 1, With('x, 2, Mul("x","x")))
assert(eval(b) == 4)
val c = With('x, 5, Add("x", With('x, 3, "x")))
assert((eval(c) == 8))
val d = With('x, 1, With('x, Add("x",1), "x"))
assert((eval(d) == 2))
```

Stößt die `eval`-Funktion auf einen Identifier, so ist dieser offensichtlich bei den bisherigen Substitutionen nicht ersetzt worden und ist frei. In diesem Fall wird also ein Fehler geworfen.

Bei der Substitution im `With`-Konstrukt darf die Substitution nur dann rekursiv im Rumpf angewendet werden, wenn der Identifier im `With` Konstrukt nicht gleich dem zu ersetzenden Identifier ist. Es muss aber immer im `xdef`-Ausdruck substituiert werden, denn auch hier sollen Identifier auftreten können, die Definition aus dem `With`-Ausdruck soll hier aber noch nicht gelten.

Der Ausdruck, durch den bei der Substitution der Identifier ersetzt wird, hat den Typ `Num`, ist also bereits vollständig ausgewertet und nicht vom Typ `Exp`. Zudem wird im `With`-Fall der `eval`-Funktion der `xdef`-Ausdruck ausgewertet, bevor die Substitution stattfindet. Die Bindung mit `With` ist also _eager_ (_call by value_). Wäre dies nicht der Fall, so können Variablen ungewollt gebunden werden (_accidental capture_) und es ist eine komplexere Implementierung notwendig.


# First-Order-Funktionen (F1-WAE)
Identifier ermöglichen Abstraktion bei mehrfach auftretenden, identischen Teilausdrücken (_Magic Literals_ :unamused:). Unterscheiden sich die Teilausdrucke aber immer an einer oder an wenigen Stellen, so sind First-Order-Funktionen notwendig, um zu abstrahieren. Die Ausdrücke `3*5+1`, `2*5+1` und `7*5+1` lassen sich etwa mit `f(x) = x*5+1` schreiben als `f(3)`, `f(2)` und `f(7)`.

First-Order-Funktionen werden über einen Bezeichner aufgerufen, können aber nicht als Parameter übergeben werden. 

Wir legen zwei Sprachkonstrukte für Funktionsaufrufe und Funktionsdefinitionen an, in einer globalen Map werden Funktionsbezeichnern Funktionsdefinitionen zugewiesen. 

```scala
case class Call(f: Symbol, args: List[Exp]) extends Exp

case class FunDef(args: List[Symbol], body: Exp)
type Funs = Map[Symbol, FunDef]
```

## Substitutionsbasierter Interpreter
Die bereits implementierten Konstanten-Identifier und die Funktions-Identifier verwenden getrennte _Namespaces_, es kann also der gleiche Bezeichner für eine Konstante und für eine Funktion verwendet werden, die Namensvergebung ist unabhängig voneinander. Es wird also in `Call` nur in den Argumenten substituiert, nicht im Funktionsnamen (denn der Funktionsname kann nicht durch einen Wert ersetzt werden):

```scala
def subst(body: Exp, i: Symbol, v: Num) : Exp = body match {
  // ...
  case Call(f,args) => Call(f, args.map(subst(_,i,v)))
}
```

Der neue Match-Zweig in `eval` ist deutlich komplizierter:
- Zuerst wird die Definition von `f` in `funs` nachgeschlagen. `args` ist die Liste der Argumente des Aufrufs mit Einträgen vom Typ `Exp`. 
- Mit `map` werden alle Argumente in der Liste vollständig ausgewertet. `vArgs` ist die Liste der ausgewerteten Argumente mit Einträgen vom Typ `Int`. 
- Als nächstes wird geprüft, dass die Argumentliste und die Parameterliste die selbe Länge besitzen, so dass bei Bedarf eine Fehlerbehandlung möglich ist.
- Nun wird für jeden Parameter in der Parameterliste eine Substitution auf dem Rumpf `fDef.body` mit dem entsprechenden Argument aus `vArgs` ausgeführt. Dies ist durch `zip` und `foldLeft` implementiert. 
- Zuletzt wird `eval` rekursiv auf dem Rumpf, in dem alle Substitutionen durchgeführt wurden, aufgerufen.

```scala
def eval(funs: Funs, e: Exp) : Int = e match {
  // ...
  case Call(f,args) => {
    val fDef = funs(f)
    val vArgs = args.map(eval(funs,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f.name)
    val substBody = fDef.args.zip(vArgs).foldLeft(fDef.body)( 
      (b,av) => subst(b, av._1, Num(av._2)) )
    // Zip list of symbols and list of integers, fold resulting list of tuples.
    // Begin with body, apply subst function for each tuple in zipped list.
    // b is preliminary folding result (body still missing substitutions), 
    // av is tuple of parameter name and corresponding value.
    eval(funs, substBody)
  }
}
```

Es ist nun möglich, nicht-terminierende Programme zu verfassen. Wird in der Definition einer Funktion die Funktion selbst aufgerufen, so entsteht eine Endlosschleife. Uns steht momentan noch kein Sprachkonstrukt zu Verfügung, um mit Abbruchbedingung Schleifen zu beenden.

```scala
val fm = Map('square -> FunDef(List('x), Mul("x","x")),
             'succ -> FunDef(List('x), Add("x",1)),
             'myAdd -> FunDef(List('x,'y), Add("x","y")),
             'forever -> FunDef(List('x), Call('forever, List("x"))))

val a = Call('square, List(Add(1,3)))
assert(eval(fm,a) == 16)
val b = Mul(2, Call('succ, List(Num(20))))
assert(eval(fm,b) == 42)
val c = Call('myAdd, List(Num(40), Num(2)))
assert(eval(fm,c) == 42)

// does not terminate
val forever = Call('forever, List(Num(0)))
```

## Umgebungsbasierter Interpreter
Unsere bisherige Implementierung von Substitution würde im Ausdruck 
```scala
With("x", 1, With("y", 2, With("z", 3, Add("x", Add("y", "z")))))
```
folgende Schritte durchlaufen:
```scala
With("y", 2, With("z", 3, Add(1, Add("y", "z"))))
With("z", 3, Add(1, Add(2, "z")))
Add(1, Add(2, 3))
```
Dabei wird der Ausdruck `Add("x", Add("y", "z"))` insgesamt drei Mal traversiert, um für jedes `With` die Substitution durchzuführen. Die Komplexität bei Ausdrücken der Länge $n$ ist also $\mathcal{O}(n^2)$. Wir suchen deshalb eine effizientere Art, um Substitution umzusetzen. 

Statt beim Auftreten eines `With`-Ausdrucks direkt zu substituieren, wollen wir uns in einer zusätzlichen Datenstruktur merken, welche Substitutionen wir im weiteren Ausdruck vornehmen müssen, so dass der zusätzliche Durchlauf wegfällt.

Hierzu verwenden wir wieder (wie in `2a-Environments.scala`) eine _Environment_, aber anstatt diese getrennt definieren zu müssen, wird sie bei der Evaluation stetig angepasst. Tritt etwa ein `With`-Ausdruck auf, so wird der entsprechende Identifier mit dem Ergebnis der zugewiesenen Expression in die Map eingetragen (die zu Beginn der Auswertung leer ist).

```scala
type Env = Map[String, Int]

def evalWithEnv(funs: FunDef, env: Env, e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => evalWithEnv(funs,env,l) + evalWithEnv(funs,env,r)
  case Mul(l,r) => evalWithEnv(funs,env,l) * evalWithEnv(funs,env,r)
  case Id(x) => env(x)
  case With(x,xdef,body) => 
    evalWithEnv(funs, env+(x -> evalWithEnv(funs,env,xdef)), body)
}
```

Das vorherige Beispiel wird nun folgendermaßen ausgewertet:
```scala
With("x", 1, With("y", 2, With("z", 3, Add("x", Add("y", "z"))))), Map()
With("y", 2, With("z", 3, Add("x", Add("y", "z")))), Map("x" -> 1)
With("z", 3, Add("x", Add("y", "z"))), Map("x" -> 1, "y" -> 2)
Add("x", Add("y", "z")), Map("x" -> 1, "y" -> 2, "z" -> 3)
```
Die Komplexität ist nun (unter der Annahme, dass die Map-Operationen in konstanter Zeit geschehen) linear zur Länge des Ausdrucks.

Das Scoping ist auch in dieser Implementation lexikalisch, da die Umgebung rekursiv weitergereicht wird und nicht global ist. Somit wird eine Bindung gilt eine Bindung nur in Unterausdrücken des bindenden Ausdruckes und nicht an anderen Stellen im Programm. Die `+`-Operation auf Maps fügt nicht nur Bindungen für neue Elemente ein, sondern ersetzt auch den Abbildungswert bei bereits enthaltenen Elementen:
```scala
var m = Map("a" -> 1)
m = m+("b" -> 2)
m = m+("a" -> 3)
m: Map[String,Int] = Map("a" -> 3, "b" -> 2)
```

Nun fehlt noch der `Call`-Fall. Hier bleiben die ersten drei Zeilen nahezu identisch, aber anstelle der aufwändigen Schleife zur Substitution im Rumpf erweitern wir einfach die _leere_ Umgebung um die Parameternamen, gebunden an die ausgewerteten Argumente:
```scala
def evalWithEnv(funs: FunDef, env: Env, e: Exp) : Int = e match {
// ...
  case Call(f,args) =>
    val fDef = funs(f)
    val vArgs = args.map(evalWithEnv(funs,env,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f)
    // ++ operator adds all tuples in a list to a map
    evalWithEnv(funs, Map()++fDef.args.zip(vArgs), fDef.body)
}
```

Wir erweitern die leere Umgebung `Map()` anstelle der bisherigen Umgebung `env`, da in unserer vorherigen Implementation auch nur für die Funktionsparameter im Funktionsrumpf substituiert wurde (und nicht für sonstige, aktuell geltende Bindungen). Die `subst`-Funktion verändert die Funktionsdefinitionen in keiner Weise und hat nicht einmal Zugriff auf diese.

:::info
**Theorem** (Äquivalenz substitutions- und umgebungsbasierter Interpretation):

Für alle `funs: Funs, e: Exp` gilt: `evalWithSubst(funs,e) = evalWithEnv(funs,Map(),e)` (wobei `evalWithSubst` die `eval`-Funktion den [substitutionsbasierten Interpreter](#Substitutionsbasierter-Interpreter) bezeichnet).
:::

Nun gäbe es aber auch die Möglichkeit, die bisherige Umgebung `env` zu erweitern und damit den Funktionsrumpf auszuwerten. In diesem Fall würden wir lokale Bindungen, die an der Stelle des `Call`-Ausdrucks gelten, in den Funktionsrumpf weitergeben. 

Die Variante mit einer neuen, leeren Umgebung wird _lexikalisches Scoping_ genannt, die Variante bei der `env` erweitert wird heißt _dynamisches Scoping_.


# Lexikalisches und dynamisches Scoping
:::info
**Lexikalisches Scoping** bedeutet, dass für ein Vorkommen eines Identifiers der Wert durch das erste bindende Vorkommen auf dem Weg vom Identifier zur Wurzel des Syntaxbaums bestimmt wird.

**Dynamisches Scoping** bedeutet, dass für ein Vorkommen eines Identifiers der Wert durch das zuletzt ausgewertete bindende Vorkommen bestimmt wird.

Bei lexikalischem Scoping ist also der Ort für die Bedeutung entscheidend, bei dynamischem Scoping der Programmzustand.
:::

Das folgende Beispiel verursacht bei lexikalischem Scoping einen Fehler, liefert aber bei dynamischem Scoping das Ergebnis `3`:
```scala
val exFunMap = Map("f" -> FunDef(List("x"), Add("x","y")))
val exExpr = With("y", 1, Call("f", List(2)))

evalWithEnv(exFunMap, Map(), exExpr)
```
Der Identifier `y` wird an den Wert `1` gebunden, bevor die Funktion `f` aufgerufen wird, in deren Rumpf `y` auftritt. `y` hat an diesem _Ort_ im Programm keine Bedeutung, es kann aber ein _Programmzustand_ vorliegen, in dem eine Bindung für `y` existiert.

Bei lexikalischem Scoping müssen Werte immer "weitergereicht" werden, während bei dynamischen Scoping alle Bindungen bei einem Funktionsaufruf automatisch im Funktionsrumpf gelten. Das automatische "Weiterreichen" kann in manchen Fällen die explizite Übergabe ersparen, führt aber in den meisten Fällen eher zu unerwarteten und unerwünschten Nebenwirkungen.

Ein Beispiel für eine Verwendung von dynamischen Scoping wäre _Exception Handling_ in Java. Wird in einem _try-catch_-Block eine Funktion `f` aufgerufen, die eine bestimmte Exception wirft, so wird beim Werfen dieser Exception über eine Art von dynamischem Scoping ermittelt, welcher ExceptionHandler zuständig ist (in dem die Ausführungshistorie durchsucht wird).


# Higher-Order-Funktionen (FAE)
Funktionen erster Ordnung erlauben die Abstraktion über sich wiederholende Muster, die an bestimmten Ausdruckspositionen variieren (z.B. eine `square`- oder eine `avg`-Funktion). Liegt aber ein Muster vor, bei dem eine Funktion variiert (z.B. bei der Komposition zweier Funktionen), so ist keine Abstraktion möglich.

Hierfür sind Higher-Order-Funktionen notwendig, es braucht eine Möglichkeit, Funktionen als Parameter zu übergeben und als Werte zu behandeln. Wir müssen also unsere Implementation anpassen, so dass Funktionen nicht als Strings, sondern als Expressions vorliegen.

Wir entfernen also das Sprachkonstrukt `Call` und ergänzen stattdessen die zwei folgenden Konstrukte:
```scala
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
```

Nun können wir Funktionen als Ausdrücke repräsentieren und benötigen somit auch keine getrennte `Funs`-Map mehr, da Funktionen nun einfach Werte sind. Solche "namenslosen" Funktionen werden typischerweise _anonyme Funktionen_ genannt, im Kontext funktionaler Sprachen auch _Lambda-Ausdrücke_.

`With` ist jetzt sogar nur noch syntaktischer Zucker, wir können bspw. `With("x", 5, Add("x",7))` ausdrücken mit `App(Fun("x", Add("x",7)), 5)`.

```scala
def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x, body), xdef)
```

Ein `Fun`-Ausdruck hat nur einen Parameter und ein `App`-Ausdruck nur ein Argument (im Gegensatz zu unserer Implementation von [First-Order-Funktionen](#First-Order-Funktionen-F1-WAE)), wir können jedoch Funktionen mit mehreren Parametern durch Currying darstellen: $f(x,y)= x+y$ entspricht $\lambda x.\lambda y.x+y$.

## Accidental Captures
Zuerst implementieren wir wieder die Version des Interpreters mit Substitutionsfunktion:
```scala
def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  // ...
  case Id(x) => if (x == i) v else e
  case Fun(param,body) =>
    if (param == i) e else Fun(param, subst(body,i,v))
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}
```

Hierbei entsteht ein neues Problem: Der zu substituierende Ausdruck `v` muss nun den Typ `Exp` besitzen, damit Identifier auch durch Funktionen (und nicht nur `Num`-Ausdrücke) substituiert werden können. Dadurch kann es aber in manchen Fällen dazu kommen, dass Identifier unbeabsichtigt gebunden werden:
```scala
val ac = subst(Fun("x", Add("x","y")), "y", Add("x",5))
```
In diesem Beispiel ist das `x` in `Add(x, 5)` nach der Substitution an den Parameter `x` der Funktion gebunden, obwohl dies vorher nicht der Fall war. Die dabei entstehende Bindung ist unerwartet, das unerwünschte "Einfangen" des Identifiers wird als _Accidental Capture_ bezeichnet und allgemein als Verletzung von lexikalischem Scoping angesehen.

:::info
Zwei Funktionen sind **alpha-äquivalent**, wenn sie bis auf den Namen des Parameters (oder der Parameter) identisch sind.
`Fun("x", Add("x",1))` und `Fun("y", Add("y",1))` sind bspw. _alpha-äquivalent_.
:::

## Capture-Avoiding Substitution
Wir nutzen Alpha-Äquivalenz, um Accidental Captures zu verhindern. Dazu brauchen wir einen "Generator", um bisher ungenutzte Namen zu erzeugen, die wir dann zur Umbenennung verwenden können.
```scala
def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names contains freshName) {
    freshName = default + last
    last += 1
  }
  freshName
}
```
Die Funktion gibt einen Namen zurück, der nicht in der Menge `names` enthalten ist. Dazu wird eine Zahl an die Eingabe `default` angehängt und schrittweise inkrementiert, bis der entstehende String nicht Element der Menge ist.

Wir benötigen außerdem eine Funktion, die alle freien Variablen in einem Ausdruck ausgibt, hier machen wir auch wieder vom Datentyp `Set` Gebrauch:
```scala
def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set.empty
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
  case Mul(l,r) => freeVars(l) ++ freeVars(r)
  case App(f,a) => freeVars(f) ++ freeVars(a)
  case Fun(x,body) => freeVars(body) - x
}

assert(freeVars(Fun("x"), Add("x","y")) == Set("y"))
```

Mithilfe dieser Funktionen können wir nun beim Substituieren Accidental Captures verhindern, man spricht hierbei von _Capture-Avoiding Substitution_:
```scala
def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Mul(l,r) => Mul(subst(l,i,v), subst(r,i,v))
  case Id(x) => if (x == i) v else Id(x)
  case Fun(param,body) =>
    if (param == i) e else {
      val fvs = freeVars(e) ++ freeVars(v) + i
      val newVar = freshName(fvs,param)
      Fun(newVar, subst(subst(body,param,Id(newVar)), i, v))
    }
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}
```

Im `Fun`-Fall prüfen wir zuerst, ob der Parameter und der zu ersetzende Identifier übereinstimmen. Ist dies der Fall, so lassen wir den `Fun`-Ausdruck unverändert. Ansonsten bestimmen wir mit `freeVars` die Menge der freien Variablen im aktuellen Ausdruck `e` sowie im einzusetzenden Ausdruck `v`. Ausgehend von dieser Menge erzeugen wir mit `freshName` einen neuen Bezeichner, mit dem wir dann den Parameternamen und alle Vorkommen des Parameternamens im Rumpf ersetzen, bevor wir die Substitution im Rumpf rekursiv fortsetzen. So ist garantiert, dass keine freien Variablen durch den Parameternamen "eingefangen" werden.

## Substitutionsbasierter Interpreter
Da Funktionen nun Werte bzw. Ausdrücke sind, muss der Interpreter auch Funktionen als Ergebnis einer Auswertung ausgeben können. Der Rückgabewert von `eval` kann also nicht mehr Int sein, stattdessen müssen wir `Exp` wählen.

Durch diesen Rückgabetyp gibt es aber auch eine neue Klasse von Fehlern, die auftreten können: Es kann passieren, dass ein `Num`-Ausdruck erwartet wird, aber ein `Fun`-Ausdruck vorliegt, etwa wenn der linke Teil eines `Add`-Ausdrucks zu einer Funktion auswertet. Auch der umgekehrte Fall kann eintreten: In einem `App`-Ausdruck wertet der linke Teil zu einem `Num`-Ausdruck anstelle einer Funktion aus.

```scala
def eval(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(param,body) => eval(subst(body,param,eval(a))) // call-by-value
    // case Fun(param,body) => eval(subst(body,param,a))    // call-by-name
    case _ => sys.error("Can only apply functions")
  }
  case _ => _
}
```

Wir könnten den Rückgabetyp auch präzisieren, in dem wir den Typ `Either[Num,Fun]` verwenden, denn es wird immer eine Zahl oder eine Funktion ausgegeben (siehe dazu `07-fae.scala`).


## Umgebungsbasierter Interpreter
Der Typ `Map[String,Int]` für die Umgebung ist nicht mehr ausreichend, da auch `Fun`-Ausdrücke gebunden werden müssen. Wir wählen also stattdessen:
```scala
type Env = Map[String,Exp]
```

Der Interpreter ergibt sich größtenteils aus der [Implementation für F1-WAE](#Umgebungsbasierter-Interpreter), kombiniert mit dem [substitutionsbasiertem Interpreter für FAE](#Substitutionsbasierter-Interpreter1):

```scala
def eval(e: Exp, env: Env) : Exp = e match {
  case Add(l,r) => (eval(l,env),eval(r,env)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l,env),eval(r,env)) match {
    case (Num(a),Num(b)) => Num(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case Id(x) => env(x)
  case App(f,a) => eval(f,env) match {
    case Fun(param, body) =>
      eval(body, Map(param -> eval(a,env)))
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}
```

Wie in [F1-WAE](#Umgebungsbasierter-Interpreter) erweitern wir bei der Rekursion in einen Funktionsrumpf die Umgebungs nicht, da sonst dynamisches Scoping vorliegen würde. In diesem Fall würde der Ausdruck

$\texttt{With}\; f = \lambda x.x+y:\;\; \texttt{With}\; y = 4:\; (f \;\; 1)$

keinen Fehler verursachen, obwohl zum Zeitpunkt der Bindung von $f$ der Bezeichner $y$ frei ist.

Stattdessen reichen wir also eine neue Umgebung weiter, die nur den Parameter und das Argument der Funktion enthält.

Dieser Interpreter ist jedoch nicht äquivalent zum [substitutionsbasierten](#Substitutionsbasierter-Interpreter1), denn der Ausdruck
```scala
val curry = App( Fun("x", App( Fun("y", Add("x","y")),2)), 3)
```
wird von `evalWithSubst` zu `Num(5)` ausgewertet, `evalWithEnv` wirft aber bei der Auswertung den Fehler `key not found: x`:

$(\lambda x.(\lambda y.x+y \;\;\; 2)\;\; 3), \; \texttt{Map()}$
$\leadsto (\lambda y.x+y \;\;\; 2), \; \texttt{Map(x -> 3)}$
$\leadsto x+y, \;\; \texttt{Map(y -> 2)}$
$\leadsto \;\; \texttt{key not found: x}$

Um dieses Problem zu umgehen, können wir nicht einfach die Umgebung im `App`-Fall erweitern, weil das wieder zu dynamischem Scoping führt. Stattdessen müssen wir uns bei der Auswertung einer Funktion sowohl die Funktion selbst, als auch die Umgebung zum Zeitpunkt der Instanziierung merken.

So ein Paar aus Funktionsdefinition und Umgebung wird _Closure_ genannt.

## Closures
Wir definieren einen neuen Typ `Value` neben `Exp`, so dass wir Ausdrücke und deren Ergebnis wieder unterscheiden können:
```scala
sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value
```

`Fun`-Ausdrücke liegen nun nach ihrer Auswertung als Closures vor, der neue Interpreter hat den Rückgabetyp `Value` und gibt Zahlen und Closures anstelle von Zahlen und Funktionen aus.

```scala
def eval(e: Exp, env: Env) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => env(x)
  case Add(l,r) => (eval(l,env),eval(r,env)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => (eval(l,env),eval(r,env)) match {
    case (NumV(a),NumV(b)) => NumV(a*b)
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => eval(f,env) match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(b, cEnv+(p -> eval(a,env)))
    case _ => sys.error("Can only apply functions")
  }
  case f@Fun(_,_) => ClosureV(f,env)
}
```

Bei der Auswertung eines `Fun`-Ausdrucks wird nun ein Closure aus dem Ausdruck und der aktuellen Umgebung erzeugt. Im `App`-Zweig verwenden wir nach der Auswertung der Funktion die Umgebung aus dem entstehenden Closure, und verwenden diese -- erweitert um die Bindung des Parameters an das ausgewertete Argument -- um den Rumpf auszuwerten.

:::info
Für alle `e: Exp` gilt: 
- `evalWithSubst(e) == Num(n)` $\Longleftrightarrow$ `evalWithEnv(e,Map()) == NumV(n)`

- Ist `evalWithSubst(e) == Fun(p,b)` und `evalWithEnv(e) == ClosureV(f,env)`, dann entspricht `Fun(p,b)` dem Ausdruck `f`, in dem für alle Bindungen in `env` Substitution durchgeführt wurde.
:::

Closures sind ein fundamental wichtiges Konzept und tauchen in der Implementation fast aller Programmiersprachen auf.

:::info
Ein **Closure** ist ein Paar, bestehend aus einer Funktionsdefinition und der Umgebung vom Zeitpunkt, als die Funktionsdefinition ausgewertet wurde.
:::

Im [substitiutionsbasiertem Interpreter](#Substitutionsbasierter-Interpreter1) wird beim Auswerten der Funktionsdefinition sofort im Rumpf substituiert. Beim [umgebungsbasiertem Interpreter](#Umgebungsbasierter-Interpreter1) muss hingegen die Umgebung zum Zeitpunkt der Auswertung gespeichert werden, so dass der Interpreter beim Erreichen der Identifier die richtigen Werte einsetzen kann.


# Mächtigkeit von FAE
Wir können in [FAE](#Higher-Order-Funktionen-FAE) nicht-terminierende Ausdrücke verfassen:
```scala
val omega = App( Fun("x", App("x","x")), Fun("x", App("x","x")) )
```
In `eval` betreten wir den `App`-Fall, dort wird substituiert, wodurch wieder `omega` entsteht.
```scala
assert(subst(App("x","x"), "x", Fun("x", App("x","x"))) ==
App(Fun("x", App("x","x")), Fun("x", App("x","x"))))
```

`omega` kann im _Lambda-Kalkül_ notiert werden als $(\lambda x.(x \; x) \;\; \lambda x.(x \; x))$, der gesamte vordere Ausdruck wird auf den hinteren Ausdruck angewendet, der hintere Ausdruck wird in den Rumpf des vorderen Ausdrucks für $x$ eingesetzt, wodurch wieder der ursprüngliche Ausdruck entsteht.

FAE ist Turing-mächtig, kann also alle Turing-berechenbaren Funktionen berechnen. Die Sprache besitzt nämlich das Turing-mächtige [Lambda-Kalkül](https://en.wikipedia.org/wiki/Lambda_calculus), das [Alonzo Church](https://en.wikipedia.org/wiki/Alonzo_Church) entwickelte, als Teilmenge.

## Church-Kodierungen
In FAE ist es bereits möglich, Listen zu repräsentieren. Die Grundidee ist es, die Liste $x_1,x_2,...,x_n$ durch $\lambda c. \lambda e. c(x_1, (... (c(x_{n-1}, c(x_n,e)))...))$ zu repräsentieren.

## Rekursion
Es ist auch möglich, Rekursion in FAE zu implementieren. Aus dem Programm `omega = (x => x x) (x => x x)` lässt sich das Programm `Y f = (x => f (x x)) (x => f (x x))` konstruieren, mit dem Schleifen kodiert werden können. Das Programm `Y` ist ein _Fixpunkt-Kombinator_. 

:::info
Ein **Fixpunkt-Kombinator** ist ein Programm, mit dem der Fixpunkt von Funktionen gebildet werden kann. 
:::

Bei der Auswertung von `Y f` entsteht ein Ausdruck der Form `(f ... (f (f (f Y)))...)`.


# Lazy Evaluation/Call-By-Name (LCFAE)
Im [substitutionsbasierten Interpreter](#Substitutionsbasierter-Interpreter1) für FAE konnten wir im `App`-Fall zwischen zwei möglichen Implementation wählen: Wir können das Argument vor der Substitution im Rumpf auswerten, oder substituieren, ohne das Argument vorher auszuwerten.

Die erste dieser _Auswertungsstrategien_ wird _Call-By-Value_ genannt, die zweite _Call-By-Name_. 

:::info
Im substitutionsbasierten Interpreter gilt für alle `e: Exp`: 

Ist `evalCBV(e) == e1` und `evalCBN(e) == e2`, dann sind `e1` und `e2` äquivalent (falls Zahlen -- identisch, falls Funktionen -- gleichbedeutend).
:::

`evalCBV(App(Fun("x",0), omega))` terminiert nicht, `evalCBN(App(Fun("x",0), omega))` liefert hingegen das Ergebnis `Num(0)`. Der Call-By-Name-Interpreter terminiert auf strikt mehr Programmen als der Call-By-Value-Interpreter.

Mit "gleichbedeutend" ist gemeint, dass sich die Funktionen gleich verhalten, aber nicht zwingend identisch sind. Für den Ausdruck $(\lambda x.(\lambda y.x+y) \;\;\; 1+1)$ würde Call-By-Value-Auswertung das Ergebnis $\lambda y.2+y$ liefern, während Call-By-Name-Auswertung das Ergebnis $\lambda y.(1+1)+y$ liefern würde. Unterscheiden sich die ausgegebenen Funktionen, so ist die Funktion im Ergebnis von `evalCBV` stets "weiter ausgewertet".

## Nutzen von Lazy Evaluation
Die Auswertungsstrategie eines Interpreters bzw. einer Programmiersprache ist nicht nur eine Frage der Effizienz, sondern hat auch Auswirkungen darauf, welche Programmstrukturen möglich sind. Unendliche Datencontainer wie Streams können bspw. erst durch _Lazy Evaluation_ implementiert werden. 

In der Sprache Haskell kann man etwa eine rekursive Funktion ohne Abbruchkriterium schreiben, die die Quadratwurzel einer Zahl annähert.

Da Haskell _lazy_ ist, kann die Funktion `rpt` ohne Abbruchbedingung definiert und mit verschiedenen Abbruchbedingungen aufgerufen werden, eine Programmstruktur die durch die Auswertungsstrategie der Sprache ermöglicht wird.

Der `evalCBN`-Interpreter erlaubt uns die Kodierung von Listen in LCFAE durch Church-Encodings (Zusatzmaterial in `08-lcfae.scala`).

## Thunks
Im substitutionsbasierten Interpreter muss nur ein Funktionsaufruf entfernt werden, um die Auswertungsstrategie zu wechseln. Im umgebungsbasierten Interpreter müssen wir dagegen einige Änderunge vornehmen.

Wir müssen bei Funktionsapplikation den Parameter in der Umgebung an das Argument ohne vorherige Auswertung binden. Dabei stoßen wir auf das gleiche Problem, das uns bei Funktionen begegnet ist: Im Argument-Ausdruck, den wir an den Parameternamen binden, müssen noch Bezeichner substituiert werden, wozu die aktuelle Umgebung benötigt wird. Diese liegt aber zum Zeitpunkt der Auswertung nicht mehr vor.

Analog zu den Closures für Funktionen brauchen wir also eine Datenstruktur, in der wir sowohl den Ausdruck, als auch die aktuelle Umgebung ablegen. Solch ein Paar aus `Exp` und `Env` nennen wir `Thunk`.

Die intutive Definition
```scala
type Thunk = (Exp, Env)
type Env = Map[String, Thunk]
```
ist in Scala durch die gegenseitige Bezüglichkeit nicht möglich. Stattdessen definieren wir den Interpreter als `trait`, wobei der Typ `Thunk` und die Funktionen `delay` und `force` abstrakt sind, so dass wir verschiedene Implementationen testen können. Wie definieren `Env` als Klasse im Trait, so dass mit dem abstrakten Type Member `Thunk` die rekursive Bezüglichkeit hergestellt werden kann. Dadurch müssen wir auch `Value` im Trait definieren. 
```scala
trait CBN {
  type Thunk

  case class Env(map: Map[String, Thunk]) {
    def apply(key: String): Thunk = map.apply(key)
    def +(other: (String, Thunk)) : Env = Env(map+other)
  }

  def delay(e: Exp, env: Env) : Thunk
  def force(t: Thunk) : Value

  sealed abstract class Value
  case class NumV(n: Int) extends Value
  case class ClosureV(f: Fun, env: Env) extends Value
  def eval(e: Exp, env: Env) : Value = e match {
    case Id(x) => force(env(x))
    case Add(l,r) =>
      (eval(l,env), eval(r,env)) match {
        case (NumV(v1),NumV(v2)) => NumV(v1+v2)
        case _ => sys.error("can only add numbers")
      }
    case App(f,a) => eval(f,env) match {
      case ClosureV(f,cEnv) => eval(f.body, cEnv + (f.param -> delay(a,env)))
      case _ => sys.error("can only apply functions")
    }
    case Num(n) => NumV(n)
    case f@Fun(x,body) => ClosureV(f,env)
  }
}
```

Die `delay`-Funktion dient dazu, die Auswertung eines Ausdrucks zu verzögern (also einen Thunk zu erzeugen), `force` dient dazu, die "aufgeschobene" Auswertung zu erzwingen (also den Ausdruck im Thunk mit der zugehörigen Umgebung auszuwerten). Im `Id`-Fall schlagen wir den Bezeichner nach und erzwingen die Auswertung des daran gebundenen Ausdrucks, im `App`-Fall verzögern wir die Auswertung des Arguments, bevor wir es in der Umgebung an den Parameter binden.

Hier die konkrete Call-By-Name-Implementierung des `CBN`-Traits:
```scala
object CallByName extends CBN {
  type Thunk = (Exp,Env)
  def delay(e: Exp, env: Env): (Exp, CallByName.Env) = (e,env)
  def force(t: Thunk): CallByName.Value = {
    println("Forcing evaluation of expression: "+t._1)
    eval(t._1,t._2)
  }
}
```




# Rekursive Bindings
Es ist nicht möglich, Rekursion folgendermaßen zu implementieren:
```scala
val facAttempt = wth("fac", 
                     Fun("n", If0("n", 1, Mul("n", App("fac", Add("n",-1))))), 
                     App("fac",4))
    
With fac = (n => If (n==1) 1 else n*fac(n-1)): fac(4)
```
Die Auswertung dieses Ausdrucks würde einen Fehler liefern, da der Bezeichner `"fac"` im Rumpf der Funktion nicht gebunden ist (also im `xDef`-Teil des With-Ausdrucks).

Um Rekursion in dieser Form zu definieren, brauchen wir ein Konstrukt, mit dem rekursive Bindungen möglich sind. Dazu führen wir das (analog zur Scheme-Funktion benannte) `Letrec`-Konstrukt ein, das die gleiche Form wie `With` bzw. `wth` hat, aber rekursive Bindings ermöglichen soll. Zudem erweitern wir die Sprache um ein `If0`-Konstrukt, um Abbruchbedingungen für rekursive Funktionen geschickt formulieren zu können:
```scala
case class If0(cond: Exp, tBranch: Exp, eBranch: Exp) extends Exp
case class Letrec(x: String, xDef: Exp, body: Exp) extends Exp

def eval(e: Exp, env: Env) : Value = e match {
  // ...
  case If0(c,t,f) => eval(c,env) match {
    case NumV(0) => eval(t,env)
    case NumV(_) => eval(f,env)
    case _ => sys.error("Can only check if number is zero")
  }
  case Letrec(i,v,b) => // ???
}
```

Es stellt sich aber die Frage, wie wir im `Letrec`-Fall vorgehen sollen. Zuerst müssen wir `xDef` auswerten, handelt es sich dabei um eine Funktion so ist das Ergebnis der Auswertung natürlich ein Closure. Die Umgebung im Closure enthält aber keine Bindung für den Funktionsnamen, der ja im Rumpf der Funktion auftritt. 

Selbst wenn man die Umgebung im Closure um eine Bindung für den Funktionsnamen erweitert, würde das nur einen rekursiven Aufruf ermöglichen, dann wäre der Funktionsname im Rumpf bereits wieder nicht gebunden. Für eine unbegrenzte Rekursionstiefe müsste für die Umgebung gelten: `env = Map("fac" -> ClosureV(...), env)`, sie müsste sich also zirkulär selbst referenzieren.

Eine Möglichkeit, in imperativen Sprachen zirkuläre Strukturen zu definieren, ist durch Objektreferenzen (bspw. durch zwei Instanzen, die gegenseitig auf sich verweisen). Hierzu ist Mutation notwendig, es wird die erste Objektinstanz mit Null-Pointer erzeugt, dann die zweite Objektinstanz mit Pointer auf die erste, zuletzt wird der Pointer im ersten Objekt mutiert und auf das zweite gesetzt. 

Wir legen eine entsprechende Datenstruktur in einem Objekt `Values` an. Diese besteht aus einem Trait `ValueHolder`, das durch die Klassen `Value` und `ValuePointer` implementiert wird. Instanzen von `Value` sind dabei selbst Werte, Instanzen von `ValuePointer` verweisen auf `Value`-Instanzen. Die `Value`-Subklassen `NumV` und `ClosureV` kennen wir bereits, der Umgebungstyp `Env` ist nun nicht mehr eine Map von `String` nach `Value` sondern von `String` nach `ValueHolder`. Damit diese zirkuläre Definition möglich ist (`ValueHolder -> Value -> ClosureV -> Env -> ValueHolder`), müssen die Definitionen in ein Objekt gepackt werden.

```scala
object Values {
  trait ValueHolder {
    def value: Value
  }
  sealed abstract class Value extends ValueHolder {
    def value = this
  }
  case class ValuePointer(var v: Value) extends ValueHolder {
    def value = v
  }
  case class NumV(n: Int) extends Value
  case class ClosureV(f: Fun, env: Env) extends Value
  type Env = Map[String,ValueHolder]
}
```
Anschließend importieren wir die Definitionen:
```scala
import Values._
```

Die `eval`-Funktion ändert sich an folgenden Stellen:
```scala
def eval(e: Exp, env: Env) : Value = e match {
  // ...
  case Id(x) => env(x).value
  // ...
  case Letrec(i,v,b) =>
    val vp = ValuePointer(null)
    val newEnv = env+(i -> vp)
    vp.v = eval(v,newEnv)
    eval(b,newEnv)
}
```

Im `Letrec`-Fall definieren wir erst den `ValuePointer` `vp`, den wir mit einem Verweis auf `null` initialisieren. Wir erweitern die aktuelle Umgebung mit der Bindung des Identifiers `i` auf `vp`. Dann mutieren wir den `ValuePointer` so, dass er das Auswertungsergebnis des zu bindenden Wertes in der neuen Umgebung referenziert (Kreisschluss!). Nach dieser Mutation ist mit der neuen Umgebung die Auswertung möglich.

Im `Id`-Fall findet die Dereferenzierung statt, wir schlagen `x` in der Environment nach und rufen auf dem Ergebnis die `value`-Methode auf, um den referenzierten `Value` zu erhalten. 


# Mutation (BCFAE)
Die Sprache FAE (auch inkl. Letrec) ist eine _rein funktionale_ Sprache, also eine Sprache ohne Mutation und Seiteneffekte. In dieser Art von Sprache lassen sich Programme besonders leicht nachvollziehen und es liegt _Referential Transparency_ vor.

:::info
**Referential Transparency** bedeutet, dass alle Aufrufe einer Funktion mit dem gleichen Argument überall durch das Ergebnis des Aufrufs ersetzt werden können, ohne die Bedeutung des Programms zu verändern.
:::

Besitzen Funktionen Seiteneffekte (etwa Print-Befehle oder Mutationen), so ist dies nicht der Fall, denn durch das Ersetzen des Funktionsaufrufs durch das Ergebnis gehen jegliche Seiteneffekte verloren. 

Die erste Form von Mutation ist das Mutieren von Variablen, also das Überschreiben des Wertes einer Variable:
```scala
var x = 1
x = 2
```
Die andere Form ist die mutierbarer Datenstrukturen, bspw. Arrays, in denen einzelne Werte überschrieben werden können. Wir wollen einfachste denkbare Form einer mutierbaren Datenstruktur unserer Sprache hinzufügen, nämlich _Boxes_. 

## Box-Container
Eine Box entspricht einem Array der Länge 1, ist also ein Datencontainer für genau einen Wert. Um Boxes zu implementieren, führen wir die folgenden Sprachkonstrukte ein:
```scala
case class NewBox(e: Exp) extends Exp
case class SetBox(b: Exp, e: Exp) extends Exp
case class OpenBox(b: Exp) extends Exp
case class Seq(e1: Exp, e2: Exp) extends Exp
```

Wir müssen Boxen instanziieren, beschreiben und auslesen bzw. dereferenzieren können, zudem brauchen wir eine Möglichkeit, um zu Sequenzieren, also die Auswertungsreihenfolge verschiedener Programmteile anzugeben. Da der Wert einer Box global mutiert werden kann, spielt die Auswertungsreihenfolge von Unterausdrücken nun eine entscheidende Rolle. 

Wir implementieren den `Box`-Container aus pädagogischen Gründen nicht durch Mutation in der Meta-Sprache, unser Interpreter soll funktional bleiben. 

```scala
val ex1 = wth("b", NewBox(0), Seq( SetBox("b", Add(1,OpenBox("b"))), OpenBox("b")))
/** Should evaluate to 1.
 * With b = NewBox(0):
 *    SetBox(b <- 1+OpenBox(b));
 *    OpenBox(b);
 */

```

Die Implementierung von `Seq` stellt uns vor eine Herausforderung, die Reihenfolge der rekursiven `eval`-Aufrufe in unserem Interpreter spielt nämlich keine Rolle, da diese Funktionsaufrufe keine Effekte haben (und auch nicht haben sollen). Wir müssen also unseren Interpreter abändern, so dass nach der Auswertung sowohl das Ergebnis, als auch durchgeführte Mutationen zurückgegeben werden, so dass wir beim Auswerten des zweiten Programmabschnitts die Effekte des ersten Programmabschnitts berücksichtigen können.

Hierzu ist es aber nicht ausreichend, `(Value,Env)` als Rückgabetyp zu wählen, wie das folgenden Beispiel zeigt:
```scala
val ex2 = wth("b", NewBox(1), 
            wth("f", Fun("x", Add("x", OpenBox("a"))),
              Seq(SetBox("a",2), App("f",5))))       
/** Should evaluate to 7.
 * With b = NewBox(1):
 *    With f = (x => x+OpenBox(b):
 *       SetBox(a, 2);
 *       f(5);
 */
```

Bei der Auswertung von `f` wird die Umgebung aus dem zu `f` gehörigen Closure verwendet. In dieser Umgebung steht in der an `"a"` gebundenen Box der Wert `1`, nicht `2`. Environments dienen zur Umsetzung von lexikalischem Scoping, sie werden entsprechend der Programmstruktur rekursiv in Unterausdrücke weitergereicht. Für Mutation ist aber die Auswertungsreihenfolge und nicht die syntaktische Struktur des Programms entscheidend, insofern sind Environments für die Implementation dieses neuen Features ungeeignet. 

Stattdessen benötigen wir eine zweite Datenstruktur, die wir als zusätzliches Argument bei der Auswertung übergeben und in evtl. modifizierter Form nach der Auswertungs ausgeben. Die neue Datenstruktur besitzt einen ganz anderen Datenfluss als die Umgebung, sie wird zwischen den Auswertungen der Unterausdrücke überreicht und nicht rekursiv in der Baumstruktur weitergegeben.

## Store und Adressen
Wir verwenden wieder unsere alten Definitionen von `Value` und `Env` und führen den Typ `Address` und `Store` ein. Zusätzlich erweitern wir `Value` um den Fall `AddressV`:
```scala
sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

type Address = Int
case class AddressV(a: Address) extends Value
type Store = Map[Address,Value]
```

Adressen sind Integers und dienen als Referenz ("Pointer") für Box-Instanzen. In der `Store`-Map werden die aktuellen Werte der Box-Instanzen hinterlegt. Um Bezeichner an Boxen binden zu können, müssen wir Boxen als `Value` repräsentieren können, wodurch `AddressV` notwendig wird.

Um neue Adressen zu erhalten, inkrementieren wir einfach die bisher höchste Adresse um 1. Um das Entfernen alter Referenzen und die Limitierungen dieses Adressen-Systems kümmern wir uns zu diesem Zeitpunkt nicht.
```scala
var address = 0
def nextAddress : Address = {
  address += 1
  address
}
```
Hier verwendet unsere Implementation doch Mutation in der Meta-Sprache, aber nur um diese Hilfsfunktion zu vereinfachen. Alternativ wäre eine Funktion `freshAddress` denkbar, die eine neue ungenutzte Adresse erzeugt (vgl. `freshName` [hier](#Capture-Avoiding-Substitution))

## Interpreter
Im Interpreter hat sich in allen Fällen was geändert, die Implementation ist generall komplexer geworden:
```scala
def eval(e: Exp, env: Env, s: Store) : (Value,Store) = e match {
  case Num(n) => (NumV(n),s)
  case Id(x) => (env(x),s)
  case f@Fun(_,_) => (ClosureV(f,env),s)
  case Add(l,r) => eval(l,env,s) match {
    case (NumV(a),s1) => eval(r,env,s1) match {
      case (NumV(b),s2) => (NumV(a+b),s2)
      case _ => sys.error("Can only add numbers")
    }
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => eval(l,env,s) match {
    case (NumV(a),s1) => eval(r,env,s1) match {
      case (NumV(b),s2) => (NumV(a*b),s2)
      case _ => sys.error("Can only multiply numbers")
    }
    case _ => sys.error("Can only multiply numbers")
  }
  case App(f,a) => eval(f,env,s) match {
    case (ClosureV(Fun(p,b),cEnv),s1) => eval(a,env,s1) match {
      case (v,s2) => eval(b, cEnv+(p -> v), s2)
    }
    case _ => sys.error("Can only apply functions")
  }
  case If0(c,t,f) => eval(c,env,s) match {
    case (NumV(0),s1) => eval(t,env,s1)
    case (NumV(_),s1) => eval(f,env,s1)
    case _ => sys.error("Can only check if number is zero")
  }
  case Seq(e1,e2) =>
    eval(e2,env,eval(e1,env,s)._2)
  case NewBox(e) => eval(e,env,s) match {
    case (v,s1) =>
      val a = nextAddress
      (AddressV(a), s1+(a -> v))
  }
  case SetBox(b,e) => eval(b,env,s) match {
    case (AddressV(a),s1) => eval(e,env,s1) match {
      case (v,s2) => (v, s2+(a -> v))
    }
    case _ => sys.error("Can only set boxes")
  }
  case OpenBox(b) =>  eval(b,env,s) match {
    case (AddressV(a),s1) => (s1(a),s1)
    case _ => sys.error("Can only open boxes")
  }
}
```

Der `Num`-, `Id`- und `Fun`-Fall entsprechen der [umgebungsbasierten FAE-Implementierung](#Umgebungsbasierter-Interpreter1) bis auf den neuen Store, der zusätzlich (mit dem Ergebnis zusammen in einem Tupel) ausgegeben wird. Im `Add`- und `Mul`-Fall müssen wir aber bereits entscheiden, in welcher Reihenfolge wir den linken und rechten Unterausdruck auswerten wollen. Wir werten (wie die meisten Sprachen) erst links und dann rechts aus. 

Durch die Auswertung des linken Teilausdrucks (mit aktueller Umgebung und aktuellem Store) erhalten wir ein Tupel aus `NumV` und `Store`, diesen neuen `Store` verwenden wir dann bei der Auswertung des rechten Teilausdrucks, so dass potentielle Mutierungen im linken Teilausdruck nun im rechten Teilausdruck berücksichtigt werden. Die Auswertung des rechten Teilausdrucks liefert wieder einen Zahlenwert und einen Store, wir geben die Summe der Zahlen und den neuesten Store als Ergebnis aus. Der `Mul`-Fall ist analog.

Auch im `App`- und `If0`-Fall müssen wir den Store von links nach rechts immer zum nächsten Unterausdruck weiterreichen, sodass bspw. Mutierungen in der Bedingung eines `If0`-Ausdrucks im Then- bzw. Else-Zweig berücksichtigt werden. 

Im `Seq`-Fall werten wir zuerst den linken Ausdruck aus und greifen mit `._2` auf den Store aus dem Ergebnis zu. Diesen nutzen wir dann bei der Auswertung des rechten Ausdrucks. Ein etwaiges Ergebnis aus dem linken Ausdruck wird also ignoriert, lediglich der rechte Ausdruck liefert ein Ergebnis. 

Um eine neue Box-Instanz zu erzeugen, werten wir zuerst den Ausdruck aus, der in der Box stehen soll. Wir erhalten einen Wert `v` und einen `Store`, erzeugen mit `nextAddress` eine neue Adresse und geben ein Tupel aus der neuen Adresse und dem Store, erweitert um die neue Adresse gebunden an den Wert `v`, aus. Im `SetBox`-Fall muss zusätzlich der Ausdruck an der ersten Stelle ausgewertet werden, um die Adresse der Box-Instanz zu erhalten und den Store mit dem neuen Wert zu aktualisieren. Im `OpenBox`-Fall wird auch erst die Adresse bestimmt, anschließend wird einfach der zugehörige Wert und der aktuelle Store ausgegeben.

Beim Auslesen einer Box-Instanz wird also erst in der Umgebung der Bezeichner nachgeschlagen, was einen `AddressV`-Wert liefern sollte, anschließend wird im Store nachgeschlagen, auf welchen Wert diese Adresse verweist. 


# Speichermanagement
Die Funktion `nextAddress`, mit der wir ungenutzte Adressen für neue Boxen erzeugen, inkrementiert einfach die Variable `address` immer weiter. Der Wert von `address` wird nach der Auswertung nicht zurückgesetzt. Während der Auswertung wird auch nicht geprüft, welche Einträge im Store noch benötigt werden und ob Adressen und die an sie gebundenen Werte entfernt werden können.

Eine Möglichkeit, nicht mehr benötigte Einträge zu entfernen, wäre ein neues Sprachkonstrukt, etwa `RemoveBox`. Damit könnte der "Programmierer" Box-Instanzen verwerfen, die nicht mehr im Programm vorkommen. Wird aber ein Identifier an eine Box-Instanz gebunden und diese Box-Instanz gelöscht, so verweist der Identifier weiterhin auf eine Adresse, für die es im Store aber keinen Eintrag mehr gibt.

Programmieren mit manuellem Speichermanagement ermöglicht performantere Programme, ist aber sehr fehleranfällig. Aus diesem Grund verwenden viele Programmiersprachen automatisches Speichermanagement in Form von _Garbage Collection_.

## Garbage Collection
Garbage Collection beruht darauf, dass algorithmisch bestimmt bzw. approximiert werden kann, welche Speicherinhalte nicht mehr oder noch benötigt werden. 

Ideal wäre ein Garbage-Collection-Algorithmus, der folgendes erfüllt:

:::info
**"Perfekte" Garbage Collection:** Wenn die Adresse $a$ im Store $s$ in der weiteren Berechnung nicht mehr benötigt wird, so wird $a$ aus $s$ entfernt. 
:::

Die Fragestellung, ob eine Adresse in der weiteren Berechnung noch benötigt wird, ist jedoch unentscheidbar, was aus der Unentscheidbarkeit des Halteproblems und dem Satz von Rice folgt. Wird bspw. eine Funktion $f$ aufgerufen und danach auf eine Adresse zugegriffen, so wird die Adresse nur benötigt, wenn $f$ terminiert. Für perfekte Garbage Collection müsste also das Halteproblem entscheidbar sein.

Auch wenn kein Algorithmus für perfekte Garbage Collection existiert, kann die Menge der noch benötigten Adressen dennoch approximiert werden. Approximinieren bedeutet dabei, das es Adressen gibt, für die keine Entscheidung möglich ist oder die falsch eingeordnet werden. Garbage Collection wird dabei so gestaltet, dass nur eine Art von Fehler geschieht, nämlich dass Adressen unnötig/fälschlicherweise im Speicher gehalten werden (aber nie fälschlicherweise verworfen werden).

:::info
**Erreichbarkeit/Reachability:** Eine Adresse ist _erreichbar_, wenn sie sich in der aktuellen Umgebung (inkl. Unterumgebungen in Closures, usw.) befindet, oder wenn es einen Pfad von Verweisen aus der aktuellen Umgebung zu der Adresse gibt. 
:::

Bei Garbage Collection handelt es sich also um das Erreichbarkeitsproblem in einem gerichteten Graphen. Voraussetzung ist dabei, dass alle nicht erreichbaren Adressen im Rest der Berechnung nicht benötigt werden. Das ist nicht der Fall, wenn durch Pointer-Arithmetik auf beliebige Adressen zugegriffen werden kann.

Da unsere Auswertungsfunktion rekursiv ist, reicht es nicht, eine Umgebung zu betrachten. Es muss für jede Instanz der `eval`-Funktion auf dem Call-Stack die zugehörigen Umgebung berücksichtigt werden, da beim Aufstieg aus den rekursiven Aufrufen wieder andere Umgebungen gelten. 

## Mark and Sweep
Die meisten einfachen Garbage-Collection-Algorithmen bestehen aus den zwei Phasen _Mark_ und _Sweep_. Im ersten Schritt werden alle Adressen markiert, die noch benötigt werden, im zweiten Schritt werden dann alle nicht markierten Adressen entfernt.

Die folgende Funktion führt Mark-And-Sweep Garbage-Collection in [BCFAE](#Mutation-BCFAE) für eine Umgebung `env` auf dem Store durch:
```scala
def gc(env: Env, store: Store) : Store = {

  def allAddrInVal(v: Value) : Set[Address] = v match {
    case NumV(_) => Set()
    case ClosureV(_,env) => allAddrInEnv(env)
    case AddressV(a) => Set(a)
  }

  def allAddrInEnv(env: Env) : Set[Address] =
    env.values.map{allAddrInVal}.fold(Set())(_++_)

  def mark(seed: Set[Address]) : Set[Address] = {
    val newAddresses = seed.flatMap(a => allAddrInVal(store(a)))
    if (newAddresses.subsetOf(seed)) seed else mark(seed++newAddresses)
  }

  val marked = mark(allAddrInEnv(env)) // mark
  
  store.filter{case (a,_) => marked(a)} // sweep

}
```

Die Funktion `allAddrInEnv` sammelt alle erreichbaren Adressen in einer Umgebung, indem `allAddrInVal` auf alle Werte in der Umgebung aufgerufen wird und die entstehenden Mengen alle vereinigt werden. Die Funktion `mark` erhält eine Adressmenge `seed` und liefert die Menge aller Adressen, die von den Adressen in `seed` aus erreicht werden können. Werden dabei keine neuen Adressen gefunden, so wird die Menge `seed` ausgegeben. Ansonsten wird `mark` rekursiv aufgerufen, dabei wird die Menge um die erreichbaren Adressen erweitert. Sie wird also schrittweise erweitert, bis keine neuen Adressen mehr gefunden werden. `gc` bestimmt erst die Menge der markierten Adressen und filtert dann den Store, so dass unmarkierte Adressen entfernt werden.

Das folgende Beispiel zeigt die Funktionsweise des Algorithmus, `gc` wird mit einer Umgebung aufgerufen, in der auf der rechten Seite die Adresse `5` vorkommt. An der Adresse `5` steht im Store eine Closure, in dem wiederum die Adresse `3` auftritt, an der Adresse `3` wird auf die Adresse `1` verwiesen. Die Adressen `2` und `5` sind nicht erreichbar und sind dementsprechend im Ergebnis aus dem Store entfernt worden.
```scala
val testEnv = Map("a" -> AddressV(5))
val testStore = Map(
  1 -> NumV(0),
  2 -> NumV(0),
  3 -> AddressV(1),
  4 -> AddressV(2),
  5 -> ClosureV(Fun("x","x"), Map("y" -> AddressV(3))))
  
// addresses 2 and 4 cannot be reached, are removed by GC
assert(gc(testEnv,testStore) ==
  Map(5 -> ClosureV(Fun("x","x"), Map("y" -> AddressV(3))),
      3 -> AddressV(1), 
      1 -> NumV(0)))
```

## Moving und Non-Moving GC
Bei automatischen Speichermanagement und Garbage Collection kann es zu einer starken Fragmentierung des Speichers kommen, denn nach wiederholter Belegung von Speicher und Garbage-Collection-Zyklen sind die belegten Speicherzellen stark verteilt und der Speicher ist "lückenhaft" befüllt.

Diese Fragmentierung erschwert zum einen die Speicherzuweisung, da größere "Datenblöcke" evtl. nicht am Stück gespeichert werden können und zerlegt werden müssen, zum anderen verschlechtert sich die Performanz, da die Speichernutzung weniger effizient wird (freier Speicher kann nicht genutzt werden, es müssen mehr Adressen im Speicher gehalten werden, zusammengehörige Daten werden nicht automatisch gemeinsam in den Cache geladen).

Um Fragmentierung zu verhindern oder zu reduzieren, müssen bei der Garbage Collection Daten verschoben ("zusammengerückt") werden, sodass die Speicherbelegung möglichst dicht bzw. kompakt bleibt. Hierbei spricht man von _Moving_ Garbage Collection.

Bei [Mark and Sweep](#Mark-and-Sweep) werden die Daten nicht verschoben, der Algorithmus ist eine Form von _Non-Moving_ Garbage Collection. Er führt allgemein über die Laufzeit hinweg zu zunehmender Fragmentierung.

Bei Moving Garbage Collection werden nicht nur die Daten im Speicher verschoben, es müssen auch alle Referenzen mit der neuen Speicheradresse aktualisiert werden. 

Ein Beispiel für Moving Garbage Collection ist die _Semi-Space Garbage Collection_. Dabei wird der Speicher in zwei Hälften geteilt, wobei während der Allokation nur eine Hälfte des Speichers verwendet wird. Ist diese voll, so wird die Garbage Collection durchgeführt: Die noch benötigten Einträge werden markiert, anschließend werden alle markierten Einträge in die freie Speicherhälfte kopiert, wo wieder am Stück allokiert wird. Zuletzt werden alle Einträge in der vollen Speicherhälfte gelöscht, das Verfahren mit umgekehrten Rollen wiederholt werden. 

**Vorteil** ist, dass bei jedem GC-Zyklus der gesamte Speicher defragmentiert wird, das Problem der steigenden Fragmentierung über Zeit ist also behoben. **Nachteile** sind die hinzukommenden Kopieroperationen und größere Anzahl an Löschoperationen, die Aktualisierung der Referenzen und der dazu notwendigen Suchoperationen, sowie die (im Worst Case) Halbierung des verfügbaren Speicherplatzes.

## Weitere Begriffe
- **Generational GC:** Es kann empirisch belegt werden, dass in den meisten Anwendungen die Objekte, die bei einem GC-Zyklus dereferenziert werden können, tendenziell sehr "jung" sind, also erst vor kurzer Zeit angelegt wurden. Bei Objekten, die sich schon sehr lange im Speicher befinden, werden viel wahrscheinlicher noch benötigt als Objekte, die erst kürzlich angelegt wurden.
&nbsp;
Bei _Generational Garbage Collection_ macht man sich diese Eigenschaft zunutze, indem die Objekte nach ihrem Alter aufgeteilt werden und im Speicherbereich der jungen Objekte öfter Garbage Collection durchgeführt wird als im Speicherbereich für alte Objekte. Somit kann Speicherplatz effizienter freigegeben werden, da gezielt die Objekte betrachtet werden, die eher dereferenziert werden können. 

- **"Stop the World"-Phänomen:** Während Garbage Collection durchgeführt wird, kann eine Anwendung i.A. nicht weiterlaufen, denn der GC-Algorithmus wäre nicht mehr sicher, wenn während der Ausführung des GC-Algorithmus weiter Adressen angelegt und Referenzen geändert werden. Stattdessen muss der Anwendungsprozess aufgeschoben werden, bis der GC-Zyklus vollendet ist. 
&nbsp;
In den meisten Fällen kann dies unbemerkt geschehen, aber bei interaktiven Programmen und Echtzeit-Anwendungen wird die Garbage Collection und das damit verbundene Aussetzen des Programms evtl. durch Ruckeln oder kurzes "Hängen" bemerkbar. Im besten Fall ist das für einen Nutzer leicht störend, im schlimmsten Fall hat die verzögerte Reaktion aber weitreichende Folgen, weshalb automatisches Speichermanagement für Programme mit extrem hohen Ansprüchen an die Reaktionsfähigkeit und Zuverlässigkeit ungeeignet sein kann. 

- **Reference Counting:** Eine andere Form des automatischen Speichermanagements, die nicht auf Erreichbarkeit von Objektinstanzen beruht, ist _Reference Counting_. Dabei wird zusammen mit jeder Objektinstanz ein Feld angelegt, in dem die Anzahl der Referenzen auf das Objekt gehalten wird. Ist diese Anzahl 0, so kann das Objekt dereferenziert werden. Bei jeder Änderung der Referenzen müssen die Felder in den betroffenen Objekten aktualisiert werden. Im Gegensatz zu Garbage Collection muss die Anwendung nicht mehr unterbrochen werden, Objekte können gelöscht werden, sobald ihr Counter 0 beträgt.
&nbsp;
Gibt es jedoch Referenzzyklen, so werden Objekte, die evtl. nicht mehr erreichbar sind, dennoch im Speicher gehalten. Deshalb wird das Verfahren typischerweise mit Zyklendetektion kombiniert, damit solche Strukturen erkannt und die Objekte korrekterweise dereferenziert werden können.


# Interpreter mit Speichermanagement
Um Garbage Collection in den BCFAE-Interpreter zu integrieren, ergänzen wir Werte um einen "Markierungszustand" und verwenden eine neue Definition für `Store`:
```scala
sealed abstract class Value {
  var marked : Boolean = false
}

trait Store {
  def malloc(stack: List[Env], v: Value) : Int
  def update(i: Int, v: Value) : Unit
  def apply(i: Int) : Value
}
```

Dabei fügt `malloc` dem Store einen Wert hinzu (wobei falls notwendig GC betrieben wird) und liefert die gewählte Adresse zurück, `update` mutiert den Wert an der Adresse `i` im Store und `apply` liest den Wert an der Adresse `i` aus. Wir verwenden jetzt also auch Mutation in der Meta-Sprache, um den Interpreter samt Speichermanagement zu implementieren. Der Store wird also nicht mehr von Funktionsaufruf zu Funktionsaufruf gereicht, sondern ist eine globale Datenstruktur, auf die von überall zugegriffen werden kann.

Unser Interpreter ähnelt dadurch wieder stärker dem [umgebungsbasierten FAE-Interpreter](#Umgebungsbasierter-Interpreter1):
```scala
def eval(e: Exp, stack: List[Env], store: Store) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => stack.head(x)
  case Add(l,r) => (eval(l,stack,store),eval(r,stack,store)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case Mul(l,r) => // analogous to Add
  case f@Fun(_,_) => ClosureV(f,stack.head)
  case If0(c,t,f) => eval(c,stack,store) match {
    case NumV(0) => eval(t,stack,store)
    case NumV(_) => eval(f,stack,store)
    case _ => sys.error("Can only check if number is zero")
  }
  case App(f,a) => eval(f,stack,store) match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(b, cEnv+(p -> eval(a,stack,store))::stack, store)
    case _ => sys.error("Can only apply functions")
  }
  case Seq(e1,e2) => eval(e1,stack,store); eval(e2,stack,store)
  case NewBox(e: Exp) =>
    val a = store.malloc(stack,eval(e,stack,store))
    AddressV(a)
  case SetBox(b: Exp, e: Exp) => eval(b,stack,store) match {
    case AddressV(a) =>
      val v = eval(e,stack,store)
      store.update(a,v)
      v
    case _ => sys.error("Can only set boxes")
  }
  case OpenBox(b: Exp) => eval(b,stack,store) match {
    case AddressV(a) => store.apply(a)
    case _ => sys.error("Can only open boxes")
  }
}
```

Statt einer Umgebung erhält der Interpreter nun eine Liste aller im Call-Stack auftretenden Umgebungen. Der Kopf der Liste ist also die aktive Umgebung des aktuellen Funktionsaufrufs, die weiteren Elemente sind die Umgebungen der "darüberliegenden" Aufrufe in der Rekursionsstruktur. Im `App`-Fall wird die Umgebungsliste erweitert, indem vorne an die Liste eine neue Umgebung angehängt wird, die zusätzlich die Bindung des Parameters enthält. Im `Id`-Fall wird der Bezeichner in der obersten/ersten Umgebung im Stack nachgeschlagen.

Im `NewBox`-, `SetBox`- und `OpenBox`-Fall werden jeweils die Store-Funktionen `malloc`, `update` und `apply` benutzt. Es muss der Stack anstelle der Umgebung als Parameter von `eval` verwendet werden, damit dieser im `NewBox`-Fall an die `malloc`-Funktion überreicht werden kann -- für GC und die Suche nach ungenutzten Adressen werden nämlich alle Umgebungen im Stack benötigt.

## Ohne GC
Eine sehr einfache Implementation der abstrakten `Store`-Klasse für Speichermanagement ohne Garbage Collection sieht folgendermaßen aus:
```scala
class StoreNoGC(size: Int) extends Store {
  val memory = new Array[Value](size)
  var nextFreeAddr: Int = 0
  def malloc(stack: List[Env], v: Value) : Int = {
    val a = nextFreeAddr
    if (a >= size) sys.error("Out of memory!")
    nextFreeAddr += 1; update(a,v); a
  }
  def update(i: Int, v: Value) : Unit = memory.update(i,v)
  def apply(i: Int) : Value = memory(i)
}
```

Der Store ist hierbei durch ein Array implementiert, zur Adressenzuweisung wird die mutierbare Variable `nextFreeAddr` verwendet und nach jeder neuen Zuweisung inkrementiert. Die Größe `size` wird bei der Instanziierung gewählt und legt fest, wie groß das Array ist, also wie viele Adressen es gibt. Sind alle Indices des Array belegt worden, so wird eine Fehlermeldung ausgegeben. Es wird nicht durch Garbage Collection versucht, Ressourcen freizugeben. 

Die folgenden Auswertungen verursacht also eine Fehlermeldung, wenn bei der Evaluation von `ex1` mindestens eine Adresse belegt wird:
```scala
val store = new StoreNoGC(2)
val stack = List[Env]()
eval(ex1,stack,store); eval(ex1,stack,store); eval(ex1,stack,store)
```

Auch ein Testprogramm, in dem mehr als zwei Boxen instanziiert werden, würde einen Fehler liefern.

## Mit GC
Wir implementieren nun die abstrakte `Store`-Klasse mit "Mark & Sweep"-Garbage-Collection. Der Store wird dabei wieder mit einer Größe `size` instanziiert, die die Anzahl der Adressen bestimmt. Wir ergänzen eine Variable `free`, in der die Anzahl freier Felder gehalten wird. Gibt es bei der Speicherallokation keine freien Adressen mehr, so wird Garbage Collection betrieben. Ist auch danach keine Adresse frei, so wird eine Fehlermeldung ausgegeben. Der verwendete "Mark & Sweep"-Algorithmus ähnelt dem [hier](#Mark-and-Sweep) aufgeführten stark:

```scala
class MarkAndSweepStore(size: Int) extends Store {
  val memory = new Array[Value](size)
  var free : Int = size
  var nextFreeAddr : Int = 0
  def malloc(stack: List[Env], v: Value) : Int = {
    if (free <= 0) gc(stack)
    if (free <= 0) sys.error("Out of memory!")
    while (memory(nextFreeAddr) != null) {
      nextFreeAddr += 1
      if (nextFreeAddr == size) nextFreeAddr = 0
    }
    update(nextFreeAddr,v); free -= 1; nextFreeAddr
  }
  def update(i: Int, v: Value) : Unit = { memory(i) = v }
  def apply(i: Int) : Value = memory(i)

  // Mark & Sweep GC:
  def allAddrInVal(v: Value) : Set[Int] = v match {
    case NumV(_) => Set()
    case AddressV(a) => Set(a)
    case ClosureV(_,env) => allAddrInEnv(env)
  }
  def allAddrInEnv(env: Env) : Set[Int] = {
    env.values.map{allAddrInVal}.fold(Set())(_++_)
  }
  def mark(seed: Set[Int]) : Unit = {
    seed.foreach(memory(_).marked = true)
    val newAddresses = 
      seed.flatMap(a => allAddrInVal(memory(a))).filter(!memory(_).marked)
    if (newAddresses.nonEmpty) {
      mark(newAddresses)
    }
  }
  def sweep() : Unit = {
    memory.indices.foreach(
      i => if (memory(i) == null) {}
      else if (memory(i).marked) memory(i).marked = false
      else { memory(i) = null; free += 1 }
    )
  }
  def gc(stack: List[Env]) : Unit = {
    mark(stack.map(allAddrInEnv).fold(Set())(_++_))
    sweep()
  }
}
```

Gibt es bei der Allokation noch (oder nach der Garbage Collection) freie Adressen, so wird das Array linear durchsucht, bis ein leeres Feld gefunden wird. Hier wird dann der Wert eingetragen, `free` wird dekrementiert und die Adresse wird ausgegeben.

Die Markierung der noch erreichbaren Adressen ist nicht mehr durch eine Menge repräsentiert, sondern durch das Feld `marked` in jedem `Value`. Die `sweep`-Funktion ersetzt nicht markierte Werte im Store durch `null` (wobei `free` inkrementiert wird) und setzt die Markierung aller Werte auf `false` zurück.


# Interpretationsarten
_Metainterpretation_ bezeichnet die Implementierung eines Sprachfeatures durch das entsprechende Feature in der Hostsprache. _Syntaktische Interpretation_ bezeichnet hingegen die Implementierung eines Features durch Reduktion auf Features niedrigerer Ebene in der Hostsprache. 

In unserer Sprache [FAE](#Higher-Order-Funktionen-FAE) ist bspw. Addition durch Metainterpretation implementiert, wir verwenden im Interpreter die Additionsfunktion von Scala und delegieren damit dieses Feature einfach an die Hostsprache. Dementsprechend besitzt Addition in unserer Sprache die gleichen Einschränkungen und Eigenschaften wie Addition in der Scala. Auch die maximale Tiefe rekursiver Programme wird nicht durch unsere Implementierung festgelegt, sondern durch Scala, da wir Rekursion in Scala für unsere Rekursion verwendet haben. Auch das Speichermanagement wird durch Scala übernommen und nicht in unserem Interpreter definiert.

Andere Sprachfeatures werden hingegen nicht durch das entsprechende Feature in der Hostsprache umgesetzt, z.B. Identifier (inkl. Scoping) oder Closures. Hier liegt syntaktische Interpretation vor. Bei der Entwerfen des Interpreters muss man sich also bewusst sein, welche Verhaltensweisen und Einschränkungen mit den Features der Hostsprache einhergehen und entscheiden, welche Features man selbst implementieren und welche man "weiterreichen" möchte. 

Man könnte Funktionen und Identifier in [FAE](#Higher-Order-Funktionen-FAE) auch vollständig durch Metainterpretation umsetzen, indem man die Definition von `Exp` folgendermaßen abändert:
```scala
sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Fun(f: Exp => Exp) extends Exp 
case class App(f: Exp, a: Exp) extends Exp
```

Eine Repräsentation, bei der Funktionen der Metasprache verwendet werden, nennt man _Higher-Order Abstract Syntax (HOAS)_.

Der Interpreter wird nun extrem einfach, aber die Kontrolle über das Verhalten von Identifiern und Bindungen (also etwa Scoping) geht verloren.
```scala
def eval(e: Exp) : Exp = e match {
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => f match {
    case Fun(f) => eval(f(eval(a)))
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}
```

Wir können auch Closures durch Metainterpretation umsetzen (s. `9b-ClosuresMetainterpretation`), hier spricht man von _Closure Conversion_. Umgekehrt wäre es auch denkbar, Zahlen und Arithmetik durch syntaktische Interpretation zu implementieren, etwa durch binäre Kodierung von Zahlen in Boolean-Arrays einer bestimmten Größe.


# Lambda-Kalkül
Entfernen wir aus [FAE](#Higher-Order-Funktionen-FAE) den `Num`- und den `Add`-Fall, so erhalten wir eine Sprache, die dem _Lambda-Kalkül_ entspricht:
```scala
sealed abstract class Exp
case class Id(name: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp
```

Das Lambda-Kalkül ist Turing-vollständig, es können darin beliebige Berechnungen ausgedrückt werden. In seiner reinen Form ist es nicht unbedingt eine praktische oder sinnvolle Sprache, dennoch ist das Lambda-Kalkül von einem theoretischen Standpunkt relevant und betrachtenswert.

In dieser Sprache gibt es keine Zahlenwerte mehr, dadurch können keine Typfehler mehr auftreten (da kein unerwarteter Werte-Typ auftreten kann):
```scala
abstract class Value
type Env = Map[String, Value]
case class ClosureV(f: Fun, env: Env)
```

Um mit dieser minimalistischen Sprache sinnvoll arbeiten zu können, sind Kodierungen für verschiedene Datentypen notwendig. Dazu verwendet man typischerweise die _Church Encodings_. Booleans werden als ihre "eigene" If-Then-Else-Funktion definiert. Darauf basierend lassen sich diverse Bool'sche Operationen definieren:
```scala
val t = Fun("t", Fun("f","t")) // true
val f = Fun("t", Fun("f","f")) // false

val ifTE = Fun("c", Fun("t", Fun("e", App(App("c","t"),"e"))))
val not = Fun("a", App(App("a", f), t)) // if a then False else True
val and = Fun("a", Fun("b", App(App("a", "b"), f))) // if a then b else False
val or = Fun("a", Fun("b", App(App("a", t), "b")))  // if a then True else b
```

Wir können auch durch eine Nullfunktion und eine Nachfolgerfunktion die Natürlichen Zahlen kodieren. Dabei wird die Zahl $n$ dargestellt durch die $n$-fache Anwendung einer Funktion $s$ auf einen Startwert $z$.
```scala
val zero = Fun("s", Fun("z","z"))
val succ = Fun("n", Fun("s", Fun("z", App("s", App(App("n", "s"), "z")))))
val one = App(succ,zero) // = Fun("s", Fun("z", App("s","z")))
val two = App(succ,one) // = Fun("s", Fun("z", App("s", App("s","z"))))
val three = App(succ,two)

val add = 
  Fun("a", Fun("b", Fun("s", Fun("z", 
    App(App("a","s"), App(App("b","s"),"z"))))))
val mul = 
  Fun("a", Fun("b", Fun("s", Fun("z", 
    App(App("a", App("b","s")), "z")))))
```

In der `succ`-Funktion wird der Ausdruck erst "ausgepackt", indem er auf das $s$ und dann auf das $z$ angewendet wird, dann wird der zusätzliche Aufruf von $s$ hinzugefügt und zuletzt wird der Ausdruck wieder zwei Mal in eine Funktion geschachtelt, um $s$ und $z$ wieder zu parametrisieren.

Bei der Addition wird der Startwert für $a$ durch $b$ ersetzt, wodurch die `succ`-Operation $a$-Mal auf $b$ durchgeführt wird, bei der Multiplikation wird $a$-Mal $b$ auf den Startwert addiert.

Durch ein Spachkonstrukt `printDot()`, der bei der Auswertung die Identitätsfunktion ausgibt und einen Punkt druckt, lassen sich durch den folgenden Ausdruck die unären Zahlenkodierungen visualisieren:
```scala
val printNum = Fun("n", App(App("n", Fun("x",printDot())), f))
```












:::success
- [x] VL 10 ab 1:00:00
- [ ] Mark & Sweep fertig zusammenfassen (???)
- [ ] Lecture Notes zu Church-Kodierungen, Fixpunkt-Kombinator
:::




