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
[Scala](https://scala-lang.org/) ist statisch getypt, funktional, sowie objekt-orientiert. Auswertung ist _eager_ (_call by value_).
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

## Mächtigkeit
Wir können in dieser Sprache nicht-terminierende Ausdrücke verfassen:
```scala
val omega = App( Fun("x", App("x","x")), Fun("x", App("x","x")) )
```
In `eval` betreten wir den `App`-Fall, dort wird substituiert, wodurch wieder `omega` entsteht.
```scala
assert(subst(App("x","x"), "x", Fun("x", App("x","x"))) ==
App(Fun("x", App("x","x")), Fun("x", App("x","x"))))
```

`omega` kann im _Lambda-Kalkül_ notiert werden als $(\lambda x.(x \; x) \;\; \lambda x.(x \; x))$, der gesamte vordere Ausdruck wird auf den hinteren Ausdruck angewendet, der hintere Ausdruck wird in den Rumpf des vorderen Ausdrucks für $x$ eingesetzt, wodurch wieder der ursprüngliche Ausdruck entsteht.

[FAE](#Higher-Order-Funktionen-FAE) ist Turing-mächtig, kann also alle Turing-berechenbaren Funktionen berechnen. Die Sprache entspricht prinzipiell dem [Lambda-Kalkül](https://en.wikipedia.org/wiki/Lambda_calculus), das [Alonzo Church](https://en.wikipedia.org/wiki/Alonzo_Church) entwickelte.

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

# Closures
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
    case ClosureV(f,cEnv) =>
      eval(f.body, cEnv+(f.param -> eval(a,env))) // call-by-value
    case _ => sys.error("Can only apply functions")
  }
  case Fun(b,p) => ClosureV(Fun(b,p),env)
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


# Call-By-Name und Call-By-Value
Im `App`-Fall wird das Argument `a` ausgewertet, bevor die Substitution durchgeführt bzw. die Bindung der Umgebung hinzugefügt wird. Diese Auswertungsstrategie wird _Call-By-Value_ genannt. Alternativ kann die Substitution/Bindung ohne vorherige Auswertung erfolgen, dann spricht man von _Call-By-Name_.

:::info
Für alle `e: Exp` gilt: Ist `evalCBV(e) == e1` und `evalCBN(e) == e2`, dann sind `e1` und `e2` äquivalent (falls Zahlen -- identisch, falls Funktionen -- gleichbedeutend).
:::

`evalCBV(App(Fun("x",0), omega))` terminiert nicht, `evalCBN(App(Fun("x",0), omega))` liefert hingegen den Ausdruck `Num(0)`. Der Call-By-Name-Interpreter terminiert auf mehr Programmen als der Call-By-Value-Interpreter.










:::success
- [x] HW 3c - Closures
- [ ] VL 6
:::




