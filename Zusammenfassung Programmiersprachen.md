---
title: Zusammenfassung Programmiersprachen
description: Programmiersprachen 1, SoSe 2020, Klauso3
langs: de

---

# Zusammenfassung Programmiersprachen 

Vorlesungsinhalte:
- gutes Verständnis von Programmiersprachen (allgemein, über _Trends_ hinweg) und deren Qualitäten, Vor- und Nachteile
- Fähigkeit, Programmiersprachen in Features zu zerlegen und diese einzeln zu verstehen und zu analysieren
- Implementieren von Programmiersprachen(-features) durch Interpreter in Scala
- Auseinandersetzung mit wenigen Compiler-bezogenen Themen
- Sprachen und deren Features, Zweck/Nutzen dieser Features, mögliche Implementationen und Vorzüge/Probleme dieser

## Scala-Grundlagen
[Scala](https://scala-lang.org/) ist statisch getypt, funktional, sowie objekt-orientiert. Auswertung ist _eager_ (_call by value_).
- **Konstanten** mit `val`, mutierbare **Variablen** mit `var`. Typ muss nicht deklariert werden, also bspw. `var n = 1` oder `var s = "abc"`. Typ kann aber auch explizit deklariert werden, also bspw. `var n: Int = 1` oder `var s: String = "abc"`.
- **Funktionen** haben die Form `def f(<arg1>: <Type1>, ...) = <body>`, Aufruf bspw. durch `f(1)`. Rückgabetyp kann optional angegeben werden: `def f(<arg1>: <Type1>, ...): <ReturnType> = <body>`
```scala
def sum(a: Int, b: Int): Int = a + b
val x = sum(5, 11)

def concat(a: String, b: String): String = a + b
val y = concat("Manfred", concat(" ", "Opel"))
```

### Datentypen
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
val sum = foldLeft(0)(_ + _)
val prod = foldLeft(1)(_ + _)
```
- **Arrays:** `Array(<first>, <second>, ...)`, Zugriff auch mit `<array>(<index>)`, mutierbar mit `<array>(<index>) = <new value>`
- **Vektoren**

### Objektorientierung
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

### Kontrollstrukturen
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

### Pattern Matching
```scala
def pm(x: Any) = x match {
  case 1 => "x is 1"
  case true => "x is true"
  case s: String => s"x is string $s"
  case (a,b,c) => s"x is tuple of $a, $b and $c"
  case _ => "x is something else"
}
```

### REPL
- REPL starten mit Befehl `scala`
- `.scala`-Datei in REPL laden mit `:load <filename>.scala`
- Ergebnisse von Auswertung werden automatisch an Variablennamen gebunden
- Bisherige Definitionen können mit `:reset` gelöscht werden
- REPL verlassen mit `:q`


### Implizite Konvertierung
Scala bietet die Möglichkeit, bestimmte Typkonvertierungsfunktionen automatisch zu nutzen, wenn so der erwartete Typ erfüllt werden kann. Dadurch können wir Ausdrücke geschickter notieren.

Hierzu muss die `implicitConversions`-Bibliothek importiert werden:
```scala
import scala.language.implicitConversions

implicit def num2exp(n: Int) : Exp = Num(n)

val test = Add(1,2) // is converted to Add(Num(1), Num(2))
```
Die Funktion `num2exp` wird durch das Keyword `implicit` automatisch auf Werte vom Typ `Int` aufgerufen, wenn an deren Stelle ein Wert vom Typ `Exp` erwartet wird.

### Typ-Alias
Mit dem Keyword `type` können neue Typen definiert werden:
```scala
type IntStringMap = Map[Int, String]
```

### Lambda-Ausdrücke und Currying
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



## Erster Interpreter 
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

### Syntaktischer Zucker und Desugaring 
In vielen Programmiersprachen gibt es prägnantere Syntax, die gleichbedeutend mit einer ausführlicheren Syntax ist (_syntaktischer Zucker_). Das erspart Schreibaufwand beim Programmieren und verbessert die Lesbarkeit von Programmen, ist aber bei der Implementierung der Sprache lästig, da man gleichbedeutende Syntax mehrfach implementieren muss. 
Der syntaktische Zucker erweitert den Funktionsumfang der Sprache nicht und jeder Ausdruck kann mit der gleichen Bedeutung ohne syntaktischen Zucker formuliert werden. Deshalb werden Sprachen typischerweise in die _Kernsprache_ und die _erweiterte Sprache_ aufgeteilt, so dass Ausdrücke vor dem Interpretieren zuerst in eine Form ohne die erweiterte Sprache gebracht werden können (_Desugaring_). So muss der Interpreter nur für die Kernsprache implementiert werden. 

### Interpreter mit Desugaring
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



### Visitor-Implementation
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


## Identifier
### Mit Environment
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

### Mit Bindings
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


## First-Order Funktionen
Identifier ermöglichen Abstraktion bei mehrfach auftretenden, identischen Teilausdrücken:
```scala

```







:::success
VL 3, 14:30
:::












