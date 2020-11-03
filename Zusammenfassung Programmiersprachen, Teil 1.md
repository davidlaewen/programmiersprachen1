---
title: Zusammenfassung Programmiersprachen, Teil 1
description: Programmiersprachen 1, SoSe 2020, Prof. Ostermann
langs: de

---

# Inhaltsverzeichnis

[TOC]


# Vorlesungsinhalte
- Verständnis von Programmiersprachen (allgemein, über aktuelle "Trends" hinweg) und deren Qualitäten, Vor- und Nachteile
- Zerlegung von Programmiersprachen in Features sowie deren Analyse
- Erlernen von strukturiertem Programmdesign durch Abstraktionen
- Befähigung, auf informierte Art Programmiersprachen einzuschätzen und zu diskutieren
- Implementieren von Programmiersprachen(-features) durch Interpreter in Scala
- Zweck/Nutzen verschiedener Sprachfeatures, mögliche Implementationen und deren Vorzüge/Probleme


# Scala-Grundlagen
[Scala](https://scala-lang.org/) ist statisch getypt, funktional und objekt-orientiert. Scala-Programme werden zu Java-Bytecode kompiliert und in der JVM ausgeführt. Auswertung ist _eager_ (_Call By Value_).
- **Konstanten** werden mit `val` und mutierbare **Variablen** mit `var` definiert. Der Typ muss dabei nicht deklariert werden (wird aber dennoch von Scala festgelegt), also bspw. `var n = 1` oder `var s = "abc"`. Der Typ kann aber auch explizit deklariert werden, also bspw. `var n: Int = 1` oder `var s: String = "abc"`.

- **Funktionen** haben die Form `def f(param: Type, ...) = body` und werden durch `f(arg,...)` aufgerufen. Der Rückgabetyp kann optional angegeben werden: 
`def f(param: Type, ...): ReturnType = body`
```scala
def square(n: Int) : Int = n*n
val x = square(4)

def concat(a: String, b: String): String = a + b
val y = concat("Heiner", concat(" ", "Hacker"))
```

## Datentypen
- Es gibt die gängigen Datentypen `Int`, `String`, `Boolean`, `Double`, etc.

- `Unit` entspricht dem Rückgabetyp `void` in Java (kein Rückgabewert, sondern "Seiteneffekt", bspw. bei der `print`-Funktion)

- **Map:** Abbildung mit Key-Value-Paaren. 
```scala
var map: Map[Int,String] = Map(1 -> "a", 2 -> "b")
assert(map(1) == "a")
```

- **Tupel:** Erlauben Gruppierung heterogener Daten. Können zwischen 2 und 22 Werte beliebigen Typs enthalten.
```scala
val t: (Int,String,Boolean) = (1, "abc", true)
val firstEntry = t._1
val (int, string, boolean) = t // binds all three fields
assert(firstEntry == int)
```

- **Listen:** Besitzen einen einheitlichen Typ, bspw. `List[Int]`, Einträge sind nicht mutierbar.
```scala
val nums = List(1,2,3)
assert(nums(0) == 1)

val nums = List.range(0, 10)
val nums = (1 to 10 by 2).toList
val letters = ('a' to 'f' by 2).toList

letters.foreach(println)
nums.filter(_ > 3).foreach(println)
val doubleNums = nums.map(_ * 2) 
val bools = nums.map(_ < 5)
val squares = nums.map(n => n*n)
val sum = nums.fold(0)(_+_)
val prod = nums.fold(1)(_*_)
```

- **Arrays:** Besitzen auch einheitlichen Typ, sind im Gegensatz zu Listen mutierbar.
```scala
val a = Array(1,2,3)
assert(a(0) == 1)
a(0) = 10
assert(a(0) == 10)
```

- **Mengen:** `Set(1,2,3)`, mutierbar mit `+` und `-` um einzelne Werte hinzuzufügen bzw. zu entfernen, `++` und `--` für Vereinigung bzw. Schnitt mit anderem `Collectible`-Datentyp, bspw. Menge oder Liste.

- **Either:** Repräsentiert einen Wert eines von zwei möglichen Typen. Jede Instanz von `Either` ist entweder eine Instanz von `Left` oder von `Right`.
```scala
val a: Either[Boolean,Int] = Left(true)
val b: Either[Boolean,Int] = Right(3)
```

## Objektorientierung
- **Klassen:**
```scala
class Person(var firstName: String, var lastName: String) {
  def sayHello() = print(s"Hello, $firstName $lastName")
}

val heiner = new Person("Heiner", "Hacker")

heiner.sayHello() // prints "Hello, Heiner Hacker"
println(heiner.firstName) // prints "Heiner"
heiner.firstName = "Heinrich" // field access without get and set methods
heiner.lastName = "Knacker"
heiner.sayHello() // now prints "Hello, Heinrich Knacker"
```
- Mit `var` definierte Felder einer Klasse sind mutierbar.
- **Abstrakte Klassen** können mit dem Keyword `abstract` angelegt werden.
- **Traits** sind Bausteine zur Konstruktion von Klassen und können nicht instanziiert werden. Sie lassen sich einer Klasse mit `with`/`extends` anfügen. 
- Ist ein Trait oder eine abstrakte Klasse _sealed_ (Keyword `sealed ...`), so müssen alle erbenden Klassen in der gleichen Datei definiert sein. Dadurch kann bei Pattern Matching erkannt werden, ob alle Fälle (also alle Case Classes) abgedeckt sind.
```scala
abstract class Speaker {
    def sayCatchPhrase(): Unit // no function body, abstract
}

trait Sleeper {
    def sleep(): Unit = println("I'm sleeping")
    def wakeUp(): Unit = println("I'm awake")
}

class Person(var name: String, catchPhrase: String) extends Speaker with Sleeper {
    def sayCatchPhrase(): Unit = println(catchPhrase)
    override def sleep() = println("Zzzzzzz")
}
```
Umdefinieren von Methoden in Unterklassen ist mit dem Keyword `override` möglich.
- **Objekte:** Können mit Keyword `object` instanziiert werden.

- Mit `extends` können Klassen/Traits erweitert oder abstrakte Klassen implementiert werden

- **Case Classes** sind hilfreich bei der Verwendung von Klassen als Datencontainer. Erzeugung von Instanzen ist ohne `new` möglich, zudem gibt es Default-Implementationen für das Vergleichen oder Hashen von Instanzen der Klasse. Mit Case Classes ist Pattern Matching möglich:
```scala
sealed abstract class UniPerson
case class Student(val id: Int) extends UniPerson
case class Professor(val subject: String) extends UniPerson

def display(p: UniPerson) : String = p match {
  case Student(id) => s"Student number $id"
  case Professor(subject) => s"Professor of $subject"
}
```

- Eine Implementation im objektorientierten Stil sieht dagegen folgendermaßen aus:
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
Die erste Variante (Pattern-Match-Dekomposition) erlaubt das Hinzufügen weiterer Funktionen, die auf dem Datentyp `UniPerson` operieren, ohne Modifikation des bestehenden Codes.

Die zweite Variante (objektorientierte Dekomposition) erlaubt das Hinzufügen weiterer Unterklassen von `UniPerson` ohne Modifikation des bestehenden Codes. 

Die Schwierigkeit, die Vorzüge beider Repräsentationen zu vereinen, wird _Expression Problem_ genannt (mehr dazu im Kapitel [Objekt-Algebren](https://pad.its-amazing.de/programmiersprachen1teil2#Expression-Problem)).

## Kontrollstrukturen
- _If-Else_-Statements:
```scala
if (1 < 2) print("Condition met")

if (a > b) {
    print("a greater than b")
} else if (a == b) {
    print("a equals b")
} else {
    print("a less than b")
}

val x = if (1 == 1) "a" else "b" // usable as ternary operator, "a" is bound to x
```

- _For-Comprehensions_: 
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
  case (a,b) if (a == b) => s"tuple ($a,$b) and $a == $b"
  case _ => "x is something else"
}
```

## REPL
- Anleitung zur Installation der Binaries [hier](https://www.scala-lang.org/download/)
- REPL lässt sich mit Befehl `scala` starten
- `.scala`-Dateien lassen sich in REPL laden mit `:load filename.scala`.
- Ergebnisse werden automatisch an Variablennamen gebunden.
- Typ eines Werts kann mit `:type` abgefragt werden
- Bisherige Definitionen können mit `:reset` gelöscht werden.
- REPL kann mit `:q` verlassen werden.


## Implizite Konvertierung
Scala bietet die Möglichkeit, bestimmte Typkonvertierungsfunktionen automatisch zu nutzen, wenn dadurch der erwartete Typ erfüllt werden kann. Mit dieser _impliziten Konvertierung_ können wir Ausdrücke für unsere Interpreter geschickter notieren.

Hierzu muss die `implicitConversions`-Bibliothek importiert werden:
```scala
import scala.language.implicitConversions

abstract class BTree
case class Node(l: BTree, r: BTree) extends BTree
case class Leaf(n: Int) extends BTree

implicit def int2btree(n: Int) : BTree = Leaf(n)

val test = Node(1,2) // is implicitly converted to Node(Leaf(1), Leaf(2))
```
Die Funktion `int2btree` wird durch das Keyword `implicit` automatisch auf Werte vom Typ `Int` aufgerufen, wenn an deren Stelle ein Wert vom Typ `BTree` erwartet wird.

## Typ-Alias
Mit dem Keyword `type` können neue Typen definiert werden:
```scala
type IntStringMap = Map[Int, String]
```

## Lambda-Ausdrücke und Currying
Es können in Scala anonyme Funktionen als Werte (_Lambda-Ausdrücke_) definiert werden. Diese haben dann einen Typ der Form `Type => ...`:
```scala
val succ : (n: Int) => n+1
```
Funktionen bzw. Lambda-Ausdrücke können dadurch der Rückgabewert von Funktionen sein (_Higher Order_), wodurch etwa _Currying_ möglich wird:
```scala
def curryAdd(n: Int) : (Int => Int) = x => x+n
assert(curryAdd(3)(4) == 7)
```


# Erster Interpreter (AE)
```scala
sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class Mul(l: Exp, r: Exp) extends Exp

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l) + eval(r)
  case Mul(l,r) => eval(l) * eval(r)
}

// example expressions
var onePlusTwo = Add(Num(1), Num(2))
assert(eval(onePlusTwo) == 3)
var twoTimesFour = Mul(Num(2), Add(Num(1), Num(3)))
assert(eval(twoTimesFour) == 8)
var threeTimesFourPlusFour = Add(Mul(Num(3),Num(4)), Num(4))
assert(eval(threeTimesFourPlusFour) == 16)
```
Bei der Implementation eines Interpreters ist ein umfassendes Verständnis der _Metasprache_ (hier Scala) notwendig, um die Eigenschaften der (von uns definierten) _Objektsprache_ vollständig zu kennen. Bspw. betreffen die Eigenschaften und Einschränkungen des `Int`-Datentyps durch dessen Verwendung auch die Objektsprache.


# Syntaktischer Zucker und Desugaring
In vielen Programmiersprachen gibt es Syntaxerweiterungen, die Programme lesbarer machen oder verkürzen, aber gleichbedeutend mit einer ausführlicheren Schreibweise sind (_syntaktischer Zucker_). Dadurch wird das Programmieren angenehmer und die Lesbarkeit von Programmen besser, für die Implementierung der Sprache ist syntaktischer Zucker jedoch lästig, da man gleichbedeutende Syntax mehrfach implementieren muss. 

Der syntaktische Zucker erweitert den Funktionsumfang der Sprache nicht und jeder Ausdruck kann mit der gleichen Bedeutung ohne syntaktischen Zucker formuliert werden. Deshalb werden Sprachen typischerweise in eine _Kernsprache_ und eine _erweiterte Sprache_ aufgeteilt. Dann können Ausdrücke vor dem Interpretieren vollständig in die Kernsprache übersetzt werden (_Desugaring_) und der Interpreter muss nur Ausdrücke in der Kernsprache auswerten können. 

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
case class SNeg(e: SExp) extends SExp
case class SSub(l: SExp, r: SExp) extends SExp

def desugar(s: SExp) : CExp = s match { 
  case SNum(n) => CNum(n)
  case SAdd(l,r) => CAdd(desugar(l), desugar(r))
  case SMul(l,r) => CMul(desugar(l), desugar(r)) 
  case SNeg(e)   => CMul(CNum(-1), desugar(e))
  case SSub(l,r) => CAdd(desugar(l), desugar(SNeg(r)))
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

Für unsere Zwecke reicht es aus, syntaktischen Zucker in einer Funktion zu definieren. Dadurch ist kein Desugaring notwendig, wir können aber trotzdem Testausdrücke einfacher notieren.
```scala
def neg(e: Exp) = Mul(Num(-1), e)
def sub(l: Exp, r: Exp) = Add(l, neg(r))
```
Syntaktischer Zucker kann (wie bei `SSub` und `sub`) auch auf anderem syntaktischen Zucker aufbauen.


# Identifier mit Umgebung (AEId)
Wir wollen unseren [ersten Interpreter](#Erster-Interpreter-AE) für arithmetische Ausdrücke um Konstantendefinitionen erweitern. Um Identifier in den Ausdrücken verwenden zu können, legen wir eine zusätzliche Datenstruktur an, nämlich eine Umgebung (_Environment_), in der Paare aus Bezeichnern und Werten hinterlegt werden.

Wir verwenden für die Identifier den Datentyp `String`, für die Umgebung definieren wir das Typ-Alias `Env`, das eine Abbildung von `String` nach `Int` bezeichnet.
```scala
import scala.language.implicitConversions

// ...
case class Id(x: String) extends Exp

implicit def num2exp(n: Int) : Exp = Num(n)
implicit def string2exp(s: String) : Exp = Id(s))

type Env = Map[String, Int]

def eval(e: Exp, env: Env) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l,env) + eval(r,env)
  case Mul(l,r) => eval(l,env) * eval(r,env)
  case Id(x) => env(x)
}

// example expressions
val exEnv = Map("x" -> 2, "y" -> 4)
val a = Add(Mul("x",5), Mul("y",7))
assert( eval(a,exEnv) == 38 )
val b = Mul(Mul("x", "x"), Add("x", "x"))
assert( eval(b,exEnv) == 16 )
```

Bei der rekursiven Auswertung der Unterausdrücke im `Add`- und `Mul`-Fall reichen wir die Umgebung `env` unverändert weiter. Um einen Identifier auszuwerten, schlagen wir ihn in der Umgebung nach und geben die mit ihm assoziierte Zahl aus.

Wir nutzen [implizite Konvertierung](#Implizite-Konvertierung), um Beispielausdrücke kompakter und lesbarer notieren zu können.


# Abstraktion durch Visitor
Eine alternative Möglichkeit, den ersten Interpreter zu definieren, ist durch _Faltung_ mit einem _internen Visitor_. Dabei handelt es sich um eine Instanz einer Klasse mit Typparameter `T`, die aus Funktionen mit den Typen `Int => T` und `(T,T) => T` besteht.

```scala
case class Visitor[T](num: Int => T, add: (T,T) => T)

def foldExp[T](v: Visitor[T], e: Exp) : T = e match {
  case Num(n) => v.num(n)
  case Add(l,r) => v.add(foldExp(v,l), foldExp(v,r))
}

val evalVisitor = new Visitor[Int](n => n, (l,r) => l+r)
val countVisitor = Visitor[Int](n => 1, (l,r) => l+r+1)
val printVisitor = Visitor[String](_.toString, "("+_+"+"+_+")")

assert( foldExp(evalVisitor, Add(2,4)) == 6 )
assert( foldExp(countVisitor, Add(2,4)) == 3 )
assert( foldExp(printVisitor, Add(2,4)) == "(2+4)")
```

Im Allgemeinen besteht ein interner Visitor für einen algebraischen Datentyp aus einer Funktion für jedes Konstrukt des Datentyps und einem Typparameter, der den Rückgabetyp der Faltung mit dem Visitor angibt. Der Typparameter wird an allen Stellen verwendet, an denen in der Definition des Datentyps der Datentyp selbst steht (im Beispiel die beiden Unterausdrücke von `Add`).

Ein wesentlicher Unterschied zur Pattern-Matching-Implementation ist, dass die Visitors selbst nicht rekursiv sind, sondern stattdessen das grundlegende Rekursionsmuster in einer Funktion abstrahiert wird (als Faltung, hier in `foldExp`). Dadurch können beliebige _kompositionale_ Operationen auf der Datenstruktur (hier `Exp`) definiert werden, ohne dass weitere Funktionen notwendig sind.

Durch die Abstraktion des Rekursionsmusters wird _Kompositionalität_ für alle Visitors erzwungen.

:::info
**Kompositionalität** heißt, dass sich die Bedeutung eines zusammengesetzten Ausdrucks aus der Bedeutung seiner Bestandteile ergibt. Bei einer rekursiven Struktur muss also die Bedeutungsfunktion strukturell rekursiv sein.

Eine Funktion ist **strukturell rekursiv**, wenn rekursive Aufrufe immer nur auf Unterausdrücken bzw. dem Inhalt der Datenfelder des aktuellen Ausdrucks stattfinden.
:::

Auch in dieser Implementation ist es möglich, eine Core-Sprache und eine erweiterte Sprache zu definieren, in dem man etwa eine zweite Klasse definiert, die `Visitor` erweitert und um zusätzliche Funktionen (bspw. `sub: (T,T) => T`) ergänzt.

Wir können auch wieder Identifier durch eine Umgebung hinzufügen, der `eval`-Visitor muss dazu mithilfe von Currying verfasst werden. Für den Typparameter `T` des Visitors muss dann `Env => Int` gewählt werden, es wird erst ein Ausdruck und anschließend eine Umgebung eingelesen, bevor das Ergebnis ausgegeben wird. Dadurch lässt sich die Funktion trotz des zusätzlichen Parameters mit der Visitor-Klasse verfassen (hier verkürzt ohne `Mul`):
```scala
case class Visitor[T](num: Int => T, add: (T,T) => T, id: String => T)

def foldExp[T](v: Visitor[T], e: Exp) : T = e match {
  case Num(n) => v.num(n)
  case Add(l,r) => v.add(foldExp(v, l), foldExp(v, r))
  case Id(x) => v.id(x)
}

val evalVisitor = Visitor[Env=>Int](
  env => _ ,
  (a, b) => env => a(env) + b(env),
  x => env => env(x))
```


# Identifier mit Bindings (WAE)
Bei der Implementation von Identifiern mit einer Environment müssen die Identifier außerhalb der Programme in der _Map_ definiert werden und Identifier können nicht umdefiniert werden. 

Besser wäre eine Implementation, bei der die Bindungen innerhalb von Programmen definiert und umdefiniert werden können. Dazu ist ein neues Sprachonstrukt, `With`, notwendig. Ein `With`-Ausdruck besteht aus einem Identifier, einem Ausdruck und einem Rumpf, in dem der Identifier an den Ausdruck gebunden ist.
```scala
case class With(x: String, xDef: Exp, body: Exp) extends Exp
```

`Add(5, With(x, 7, Add(x, 3)))` soll also bspw. zu `15` auswerten.

Die Bindung soll nur im Rumpf gelten, nicht außerhalb (_lexikalisches Scoping_).

Dazu soll die Definition von `x` (hier `7`) ausgewertet und für alle Vorkommen von `x` im Rumpf eingesetzt werden (_Substitution_). Hierfür definieren wir eine neue Funktion `subst` mit dem Typ `(Exp, String, Num) => Exp`. Es müssen zwei verschiedene Vorkommen von Identifiern unterschieden werden: _Bindende_ Vorkommen (Definitionen in `With`-Ausdrücken) und _gebundene_ Vorkommen (Verwendung an allen anderen Stellen). Tritt ein Identifier auf, ohne dass es "weiter außen" im Ausdruck ein bindendes Vorkommen gibt, so handelt es sich um ein _freies_ Vorkommen. 

```scala
With x = 7: // binding occurence
  x + y // x bound, y free
```

:::info
Der **Scope** (Sichtbarkeitsbereich) eines bindenden Vorkommens des Identifiers `x` ist die Region des Programmtextes in dem sich Vorkommen von `x` auf das bindende Vorkommen beziehen.
:::

Beim Entwerfen der Sprache muss das erwünschte Scoping-Verhalten entschieden und implementiert werden.
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
Würde das Scope des ersten bindenden Vorkommens den gesamten Ausdruck umfassen, so wäre es nicht möglich, Identifier umzubinden, außerdem sind Programme so weniger verständlich und deren Auswertung schlechter nachvollziehbar, da Unterausdrücke je nach Kontext zu einem anderen Ergebnis auswerten würden.

Es ergibt sich die folgende Implementation für die Erweiterung um `With` und Substitution:
```scala
case class With(x: String, xDef: Exp, body: Exp) extends Exp

def subst(body: Exp, i: String, v: Num) : Exp = body match {
  case Num(_) => body
  case Id(x) => if (x == i) v else body
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case With(x,xDef,body) =>  // do not substitute in body if x is redefined
    With(x, subst(xDef,i,v), if (x == i) body else subst(body,i,v))
}

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x.name)
  case Add(l,r) => eval(l) + eval(r)
  case With(x,xDef,body) => eval(subst(body,x,Num(eval(xDef))))
}

val a = Add(5, With("x", 7, Add("x", 3)))
assert(eval(a) == 15)
val b = With("x", 1, With("x", 2, Add("x","x")))
assert(eval(b) == 4)
val c = With("x", 5, Add("x", With("x", 3, "x")))
assert((eval(c) == 8))
val d = With("x", 1, With("x", Add("x",1), "x"))
assert((eval(d) == 2))
```

Stößt die `eval`-Funktion auf einen Identifier, so ist dieser offensichtlich bei den bisherigen Substitutionen nicht ersetzt worden und ist frei. In diesem Fall wird also ein Fehler geworfen. Im `With`-Fall wird `subst` aufgerufen, um im Rumpf (`body`) die Vorkommen von `x` durch `xDef` zu ersetzen. Dabei wird `xDef` zuerst ausgewertet, was einen Wert vom Typ `Int` ergibt, und anschließend wieder mit `Num()` "verpackt", damit der erwartete Typ erfüllt ist und die Zahl als Unterausdruck eingefügt werden kann.

Bei der Substitution im `With`-Konstrukt darf die Substitution nur dann rekursiv im Rumpf angewendet werden, wenn der Identifier im `With` Konstrukt nicht gleich dem zu ersetzenden Identifier ist. In diesem Fall beziehen sich Vorkommen des Identifiers im Rumpf nämlich auf die Bindung im `With`-Konstrukt und nicht auf die Bindung, für die die Substitution durchgeführt wird.

Es muss aber immer im `xDef`-Ausdruck substituiert werden, denn auch hier können Identifier auftreten, die Definition aus dem `With`-Ausdruck soll aber nur im Rumpf und nicht in `xDef` gelten.


# First-Order-Funktionen (F1-WAE)
Identifier ermöglichen Abstraktion bei mehrfach auftretenden, identischen Teilausdrücken (_"Magic Literals"_) -- bspw. kann damit eine Konstante gebunden werden, die in einer Berechnung häufig vorkommt. 

Unterscheiden sich die Teilausdrücke aber immer an einer oder an wenigen Stellen, so sind First-Order-Funktionen notwendig, um zu abstrahieren. Die Ausdrücke `5*3+1`, `5*2+1` und `5*7+1` lassen sich etwa mit `f(x) = 5*x+1` schreiben als `f(3)`, `f(2)` und `f(7)`. Weitere Beispiele dieser Abstraktion wären die Funktionen `square(n) = n*n` und `avg(x,y) = (x+y)/2`.

First-Order-Funktionen werden über einen Bezeichner aufgerufen, können aber nicht als Parameter übergeben werden und sind nicht Ausdrücke (Typ `Exp`).

Wir legen zwei Sprachkonstrukte für Funktionsaufrufe und Funktionsdefinitionen an. Aufrufe sind Expressions und bestehen aus dem Bezeichner der Funktion sowie einer Liste von Argumenten, Definitionen bestehen aus einer Liste von Parametern und einem Rumpf. In einer globalen Map werden Funktionsbezeichnern Funktionsdefinitionen zugewiesen, die Funktionen werden also außerhalb des Programms definiert (im Gegensatz zu Bindungen mit `With`). 
```scala
case class Call(f: String, args: List[Exp]) extends Exp

case class FunDef(params: List[String], body: Exp)
type Funs = Map[String, FunDef]
```

## Substitutionsbasierter Interpreter
Die bereits implementierten Bindungen mit `With` und die Funktionen verwenden getrennte _Namespaces_, es kann also der gleiche Bezeichner für eine Konstante und für eine Funktion verwendet werden, die Namensvergabe ist unabhängig voneinander. 

Wir erweitern `subst` um den `Call`-Fall, dabei wird die Substitution mit der `map`-Funktion auf alle Funktionsargumente angewandt.

```scala
def subst(body: Exp, i: String, v: Num) : Exp = body match {
  // ...
  case Call(f,args) => Call(f, args.map(subst(_,i,v)))
}
```

In `eval` überreichen wir zusätzlich immer die Map, in der Funktionen definiert sind. Der neue Match-Zweig ist hier deutlich komplizierter:
```scala
def eval(e: Exp, funs: Funs) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier "+x)
  case Add(l,r) => eval(l,funs)+eval(r,funs)
  case With(x,xDef,b) => eval(subst(b,x,Num(eval(xDef,funs))), funs)
  case Call(f,args) => {
    val fDef = funs(f)
    val vArgs = args.map(eval(_,funs))
    if (fDef.params.size != vArgs.size)
      sys.error("Incorrect number of args in call to "+f)
    val substBody = fDef.params.zip(vArgs).foldLeft(fDef.body)(
      (b,pv) => subst(b, pv._1, Num(pv._2))
    )
    eval(substBody,funs)
  }
}
```
`f` ist ein Bezeichner vom Typ `String`, `args` ist die Liste der Argumente des Aufrufs vom Typ `List[Exp]`. 

Zuerst wird die Definition von `f` in `funs` nachgeschlagen und das Ergebnis an `fDef` gebunden. Mit `map` werden dann alle Argumente in der Liste vollständig ausgewertet. `vArgs` ist die Liste der ausgewerteten Argumente mit Einträgen vom Typ `Int`. Als nächstes wird geprüft, dass die Argumentliste `vArgs` und die Parameterliste `fDef.params` die selbe Länge besitzen, ist dies nicht der Fall, so wird ein Fehler geworfen.

Nun wird für jeden Parameter in der Parameterliste die Substitution im Rumpf `fDef.body` mit dem entsprechenden Argument aus `vArgs` ausgeführt. Dies ist durch `zip` und `foldLeft` implementiert. `fDef.params.zip(vArgs)` erzeugt eine Liste vom Typ `List[(String,Int)]`, in der jeder Parameter mit dem korrespondierenden Argument in einem Tupel vorliegt. `foldLeft` erhält `body` als Startwert und wendet dann (vom Ende der Liste beginnend) nacheinander für jedes Tupel die entsprechende Substitution an. Es werden also Aufrufe von `subst` geschachtelt, wobei die innereste Substitution auf dem ursprünglichen Rumpf `body` ausgeführt wird.

Zuletzt wird `eval` rekursiv auf dem Rumpf, in dem alle Substitutionen durchgeführt wurden, aufgerufen.

Es ist nun möglich, nicht-terminierende Programme zu verfassen. Wird in der Definition einer Funktion die Funktion selbst aufgerufen, so entsteht eine Endlosschleife.

```scala
val fm = Map("square" -> FunDef(List("x"), Mul("x","x")),
             "succ" -> FunDef(List("x"), Add("x",1)),
             "myAdd" -> FunDef(List("x","y"), Add("x","y")),
             "forever" -> FunDef(List("x"), Call("forever", List("x"))))

val a = Call("square", List(Add(1,3)))
assert(eval(fm,a) == 16)
val b = Mul(2, Call("succ", List(Num(20))))
assert(eval(fm,b) == 42)
val c = Call("myAdd", List(Num(40), Num(2)))
assert(eval(fm,c) == 42)

val forever = Call("forever", List(Num(0)))
// eval(fm,forever) does not terminate
```

## Umgebungsbasierter Interpreter
Unsere bisherige Implementierung von Substitution würde im Ausdruck 
```scala
With("x", 1, With("y", 2, With("z", 3, Add("x", Add("y", "z")))))
```
folgende Schritte durchlaufen:
```scala
With("y", 2, With("z", 3, Add(1, Add("y", "z"))))
// ~~>
With("z", 3, Add(1, Add(2, "z")))
// ~~>
Add(1, Add(2, 3))
```
Dabei wird der Ausdruck `Add("x", Add("y", "z"))` insgesamt drei Mal traversiert, um für jedes `With` die Substitution durchzuführen. Die Komplexität bei Ausdrücken der Länge $n$ ist $\mathcal{O}(n^2)$. Wir suchen deshalb eine effizientere Art, um Substitution umzusetzen. 

Statt beim Auftreten eines `With`-Ausdrucks direkt zu substituieren, wollen wir uns in einer zusätzlichen Datenstruktur merken, welche Substitutionen wir im weiteren Ausdruck vornehmen müssen, so dass der zusätzliche Durchlauf wegfällt.

Hierzu verwenden wir wieder (wie in [AEId](#Identifier-mit-Umgebung-AEId)) eine _Umgebung_, diese wird aber nicht getrennt und global definiert, sondern bei der Evaluation stetig angepasst. Tritt etwa ein `With`-Ausdruck auf, so wird der entsprechende Identifier mit dem Auswertungsergebnis der zugewiesenen Expression in die Map eingetragen (die zu Beginn der Auswertung leer ist).

```scala
type Env = Map[String, Int]

def evalWithEnv(funs: FunDef, env: Env, e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => env(x)
  case Add(l,r) => evalWithEnv(funs,env,l) + evalWithEnv(funs,env,r)
  case With(x,xDef,body) => 
    evalWithEnv(funs, env+(x -> evalWithEnv(funs,env,xDef)), body)
}
```

Da nun die notwendigen Substitutionen nicht direkt ausgeführt, sondern in der Umgebung hinterlegt und "aufgeschoben" werden, können wir auf nicht-substituierte Identifier stoßen. Diese schlagen wir dann in der Umgebung `env` nach, um sie durch den Wert zu ersetzen, an den sie gebunden sind.

Das vorherige Beispiel wird nun folgendermaßen ausgewertet:
```scala
With("x", 1, With("y", 2, With("z", 3, Add("x", Add("y", "z"))))), Map()
// ~~>
With("y", 2, With("z", 3, Add("x", Add("y", "z")))), Map("x" -> 1)
// ~~>
With("z", 3, Add("x", Add("y", "z"))), Map("x" -> 1, "y" -> 2)
// ~~>
Add("x", Add("y", "z")), Map("x" -> 1, "y" -> 2, "z" -> 3)
```
Die Komplexität ist nun (unter der Annahme, dass die Map-Operationen in konstanter Komplexität haben) linear in Abhängigkeit von der Länge des Ausdrucks.

Das Scoping ist auch in dieser Implementation lexikalisch, da die Umgebung rekursiv weitergereicht wird und nicht global ist. Somit gilt eine Bindung nur in Unterausdrücken des bindenden Ausdruckes und nicht an anderen Stellen im Programm. Die `+`-Operation auf Maps fügt nicht nur Bindungen für neue Elemente ein, sondern ersetzt auch den Abbildungswert bei bereits enthaltenen Elementen:
```scala
var m = Map("a" -> 1)
m = m+("b" -> 2)
m = m+("a" -> 3)
// ~~>
m: Map[String,Int] = Map("a" -> 3, "b" -> 2)
```

Nun fehlt noch der `Call`-Fall. Hier bleiben die ersten drei Zeilen nahezu identisch, aber anstelle der Faltung zur Substitution im Rumpf erweitern wir einfach die leere Umgebung um die Parameternamen, gebunden an die ausgewerteten Argumente:
```scala
def evalWithEnv(funs: FunDef, env: Env, e: Exp) : Int = e match {
// ...
  case Call(f,args) =>
    val fDef = funs(f)
    val vArgs = args.map(evalWithEnv(funs,env,_))
    if (fDef.args.size != vArgs.size)
      sys.error("Incorrect number of params in call to " + f)
    evalWithEnv(funs, Map()++fDef.args.zip(vArgs), fDef.body)
}
```

Dabei können mit dem Operator `++` die Tupel in der Liste der Map hinzugefügt werden.

Wir erweitern die leere Umgebung `Map()` anstelle der bisherigen Umgebung `env`, da in unserer vorherigen Implementation auch nur für die Funktionsparameter im Funktionsrumpf substituiert wurde (und nicht für sonstige, aktuell geltende Bindungen). Die `subst`-Funktion verändert nämlich die Funktionsdefinitionen in keiner Weise und hat nicht einmal Zugriff auf diese.

:::info
**Äquivalenz des substitutions- und umgebungsbasierten Interpreters:**

Für alle `funs: Funs, e: Exp` gilt: `evalWithSubst(funs,e) = evalWithEnv(funs,Map(),e)`.
:::

Nun gäbe es aber auch die Möglichkeit, die bisherige Umgebung `env` zu erweitern und damit den Funktionsrumpf auszuwerten. In diesem Fall würden wir lokale Bindungen, die an der Stelle des `Call`-Ausdrucks gelten, in den Funktionsrumpf weitergeben. 

Die Variante mit einer neuen, leeren Umgebung wird _lexikalisches Scoping_ genannt, die Variante bei der `env` erweitert wird heißt _dynamisches Scoping_.


# Lexikalisches und dynamisches Scoping
:::info
**Lexikalisches (oder statisches) Scoping** bedeutet, dass der Scope eines bindenden Vorkommens syntaktisch beschränkt ist, bspw. auf einen Funktionsrumpf. In unserer Sprache wird für ein Vorkommen eines Identifiers der Wert durch das erste bindende Vorkommen auf dem Weg vom Identifier zur Wurzel des abstrakten Syntaxbaums (AST) bestimmt.

**Dynamisches Scoping** bedeutet, dass für ein Vorkommen eines Identifiers der Wert durch das zuletzt ausgewertete bindende Vorkommen bestimmt wird. Eine Bindung gilt dadurch während der gesamten weiteren Programmausführung und ist nicht auf einen bestimmten Bereich im Programms beschränkt.

Bei lexikalischem Scoping ist also der Ort für die Bedeutung entscheidend, bei dynamischem Scoping der Programmzustand.
:::

Das folgende Beispiel verursacht bei lexikalischem Scoping einen Fehler, liefert aber bei dynamischem Scoping das Ergebnis `3`:
```scala
val exFunMap = Map("f" -> FunDef(List("x"), Add("x","y")))
val exExpr = With("y", 1, Call("f", List(2)))

evalWithEnv(exFunMap, Map(), exExpr)
```
Der Identifier `y` wird an den Wert `1` gebunden, bevor die Funktion `f` aufgerufen wird, in deren Rumpf `y` auftritt. `y` hat an diesem _Ort_ im Programm keine Bedeutung, es kann aber ein _Programmzustand_ vorliegen, in dem eine Bindung für `y` existiert.

Bei lexikalischem Scoping müssen Werte immer "weitergereicht" werden, während bei dynamischen Scoping alle Bindungen zum Zeitpunkt eines Funktionsaufrufs auch im Funktionsrumpf gelten. Diese automatische Weitergabe kann in manchen Fällen ein explizites Überreichen ersparen, führt aber in den meisten Fällen eher zu unerwarteten und unerwünschten Nebenwirkungen.

Ein Beispiel für eine Verwendung von dynamischem Scoping wäre _Exception Handling_ in Java. Wird in einem _try-catch_-Block eine Funktion `f` aufgerufen, die eine bestimmte Exception wirft, so wird beim Werfen dieser Exception über eine Art von dynamischem Scoping ermittelt, welcher ExceptionHandler zuständig ist (in dem die Ausführungshistorie durchsucht wird).


# Higher-Order-Funktionen (FAE)
Funktionen erster Ordnung erlauben die Abstraktion über sich wiederholende Muster, die an bestimmten Ausdruckspositionen variieren (z.B. eine `square`- oder `avg`-Funktion). Liegt aber ein Muster vor, bei dem eine Funktion variiert (z.B. bei der Komposition zweier Funktionen), so ist keine Abstraktion möglich.

Hierfür sind Higher-Order-Funktionen notwendig, es braucht eine Möglichkeit, Funktionen als Parameter zu übergeben, als Ergebnis zurückzugeben und als Werte zu behandeln. Wir müssen also unsere Implementation anpassen, so dass Funktionen nicht als ein vom Programm getrenntes Konstrukt, sondern als Expressions vorliegen.

Wir entfernen also das Sprachkonstrukt `Call` und ergänzen stattdessen die zwei folgenden Konstrukte:
```scala
case class Fun(param: String, body: Exp) extends Exp
case class App(funExpr: Exp, argExpr: Exp) extends Exp
```

Nun können wir Funktionen direkt im Programm definieren und binden, wodurch wir keine getrennte `Funs`-Map mehr benötigen. Funktion sind eine Form von Werten, solche "namenslosen" Funktionen werden typischerweise _anonyme Funktionen_ genannt, im Kontext funktionaler Sprachen auch _Lambda-Ausdrücke_.

`With` ist jetzt sogar nur noch syntaktischer Zucker, wir können bspw. `With("x", 5, Add("x",7))` ausdrücken mit `App(Fun("x", Add("x",7)), 5)`.

```scala
def wth(x: String, xdef: Exp, body: Exp) : Exp = App(Fun(x, body), xdef)
```

Ein `Fun`-Ausdruck hat nur einen Parameter und ein `App`-Ausdruck nur ein Argument (im Gegensatz zu unserer Implementation von [First-Order-Funktionen](#First-Order-Funktionen-F1-WAE)), wir können jedoch Funktionen mit mehreren Parametern durch Currying darstellen: $f(x,y)= x+y$ entspricht $f(x)(y) = x+y$ bzw. in der Notation des Lambda-Kalküls $\lambda x.\lambda y.x+y$.

## Accidental Captures
Zuerst implementieren wir den Interpreter wieder durch Substitution:
```scala
def subst(e: Exp, i: String, v: Exp) : Exp = e match {
  case Num(_) => e
  case Id(x) => if (x == i) v else e
  case Add(l,r) => Add(subst(l,i,v), subst(r,i,v))
  case Fun(p,b) =>
    if (param == i) e else Fun(p, subst(b,i,v))
  case App(f,a) => App(subst(f,i,v), subst(a,i,v))
}
```

Hierbei entsteht ein neues Problem: Der zu substituierende Ausdruck `v` muss nun den Typ `Exp` besitzen, damit Identifier auch durch Funktionen (und nicht nur `Num`-Ausdrücke) substituiert werden können. Dadurch kann es aber in manchen Fällen dazu kommen, dass Identifier unbeabsichtigt gebunden werden:
```scala
val ac = subst(Fun("x", Add("x","y")), "y", Add("x",5))
```
In diesem Beispiel ist das `x` in `Add(x, 5)` nach der Substitution an den Parameter `x` der Funktion gebunden, obwohl dies vorher nicht der Fall war. Die dabei entstehende Bindung ist unerwartet, dieses unerwünschte "Einfangen" eines Identifiers wird als _Accidental Capture_ bezeichnet und allgemein als Verletzung von lexikalischem Scoping angesehen.

## Capture-Avoiding Substitution
:::info
Zwei Funktionen sind **alpha-äquivalent**, wenn sie bis auf den Namen des Parameters (oder der Parameter) identisch sind.
`Fun("x", Add("x",1))` und `Fun("y", Add("y",1))` sind bspw. _alpha-äquivalent_.
:::

Wir nutzen Alpha-Äquivalenz, um Namenskonflikte und damit Accidental Captures zu verhindern. Dabei wählen wir für den Parameter einer Funktion einen neuen Namen, der weder im zu substituierenden Ausdruck noch im aktuellen Ausdruck ungebunden auftritt. Wir brauchen also einen "Generator", um bisher ungenutzte Namen zu erzeugen, die wir dann zur Umbenennung verwenden können.
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

Die Funktion gibt einen Namen zurück, der nicht in der Menge `names` enthalten ist. Ist `default` nicht in der Menge enthalten, so wird `default` ausgegeben, ansonsten wird eine Zahl an die Eingabe `default` angehängt und schrittweise inkrementiert, bis der entstehende String nicht Element der Menge ist.

Wir benötigen außerdem eine Funktion, die die Menge aller freien Variablen in einem Ausdruck ausgibt, dazu verwenden wir den Datentyp `Set`:
```scala
def freeVars(e: Exp) : Set[String] = e match {
  case Num(_) => Set.empty
  case Id(x) => Set(x)
  case Add(l,r) => freeVars(l) ++ freeVars(r)
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
  case Id(x) => if (x == i) v else e
  case Fun(p,b) =>
    if (p == i) e else {
      val fvs = freeVars(e) ++ freeVars(v) + i
      val nn = freshName(fvs,p)
      Fun(nn, subst(subst(b,p,Id(nn)), i, v))
    }
  case App(f: Exp, a: Exp) => App(subst(f,i,v), subst(a,i,v))
}
```

Im `Fun`-Fall prüfen wir zuerst, ob der Parameter und der zu ersetzende Identifier übereinstimmen. Ist dies der Fall, so lassen wir den `Fun`-Ausdruck unverändert, da der Rumpf nicht im Scope des bindenden Vorkommens liegt, für das substituiert wird. 

Ansonsten bestimmen wir mit `freeVars` die Menge der freien Variablen im aktuellen Ausdruck `e` sowie im einzusetzenden Ausdruck `v` und vereinigen diese zusammen mit `i` (damit nicht `i` als neuer Name gewählt und fälschlicherweise substituiert wird, falls `i` nicht in `e` frei vorkommt). Ausgehend von dieser Menge erzeugen wir mit `freshName` einen neuen Bezeichner, mit dem wir dann den Parameternamen und alle Vorkommen des Parameternamens im Rumpf ersetzen, bevor wir die Substitution im Rumpf rekursiv fortsetzen. So ist garantiert, dass keine freien Variablen durch die Funktion "eingefangen" werden.

## Substitutionsbasierter Interpreter
Da Funktionen nun Werte bzw. Ausdrücke sind, muss der Interpreter auch Funktionen als Ergebnis einer Auswertung ausgeben können. Der Rückgabewert von `eval` kann also nicht mehr `Int` sein, stattdessen müssen wir `Exp` wählen.

Durch diesen Rückgabetyp gibt es aber auch eine neue Klasse von Fehlern, die auftreten können: Es kann passieren, dass eine Zahl erwartet wird, aber eine Funktion vorliegt, etwa wenn der linke Teil eines `Add`-Ausdrucks zu einer Funktion auswertet. Auch der umgekehrte Fall kann eintreten: In einem `App`-Ausdruck wertet der linke Teil zu einer Zahl anstelle einer Funktion aus.

```scala
def eval(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (eval(l),eval(r)) match {
    case (Num(a),Num(b)) => Num(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(param,body) => eval(subst(body,param,eval(a))) // call-by-value
    // case Fun(param,body) => eval(subst(body,param,a))    // call-by-name
    case _ => sys.error("Can only apply functions")
  }
  case _ => e
}
```

Aus diesem Grund müssen wir im `Add`- und `App`-Fall erst beide Unterausdrücke auswerten und dann via Pattern-Matching prüfen, ob jeweils der korrekte Typ vorliegt. Ist dies nicht der Fall, so werfen wir einen Fehler. `Num`- und `Fun`-Ausdrücke werten zu sich selbst aus, die Ausgabe des Interpreters ist immer vom Typ `Num` oder `Fun`.

Wir könnten den Rückgabetyp also mit `Either[Num,Fun]` präzisieren ([mögliche Implementation](https://github.com/DavidLaewen/programmiersprachen1/blob/master/material/additional-material/FAEWithEitherType.scala)).

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
  case Id(x) => env(x)
  case App(f,a) => eval(f,env) match {
    case Fun(p,b) =>
      eval(b, Map(p -> eval(a,env)))
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

Um dieses Problem zu umgehen, können wir nicht einfach die Umgebung im `App`-Fall erweitern, weil das (wie bereits gezeigt) zu dynamischem Scoping führen würde. Stattdessen müssen wir uns bei der Auswertung einer Funktion sowohl die Funktion selbst, als auch die Umgebung zum Zeitpunkt der Instanziierung merken.

So ein Paar aus Funktion und Umgebung wird _Closure_ genannt.

## Closures
Wir definieren einen neuen Typ `Value` neben `Exp`, so dass wir Ausdrücke und die Ergebnisse deren Auswertung wieder unterscheiden können:
```scala
sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value
```

`Fun`-Ausdrücke liegen nun nach ihrer Auswertung als Closures vor, der neue Interpreter hat den Rückgabetyp `Value` und gibt Zahlen (`NumV`) und Closures (`ClosureV`) anstelle von Zahlen (`Num`) und Funktionen (`Fun`) aus.

```scala
def eval(e: Exp, env: Env) : Value = e match {
  case Num(n) => NumV(n)
  case Id(x) => env(x)
  case Add(l,r) => (eval(l,env),eval(r,env)) match {
    case (NumV(a),NumV(b)) => NumV(a+b)
    case _ => sys.error("Can only add numbers")
  }
  case f@Fun(_,_) => ClosureV(f,env)
  case App(f,a) => eval(f,env) match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(b, cEnv+(p -> eval(a,env)))
    case _ => sys.error("Can only apply functions")
  }
}
```

Bei der Auswertung eines `Fun`-Ausdrucks wird nun ein Closure aus dem Ausdruck und der aktuellen Umgebung erzeugt. Im `App`-Zweig verwenden wir nach der Auswertung der Funktion die Umgebung aus dem entstehenden Closure -- erweitert um die Bindung des Parameters an das ausgewertete Argument -- um den Rumpf auszuwerten.

Durch das "Aufschieben" der notwendigen Substitutionen in der Umgebung ist es also im Fall von Funktionen notwendig geworden, die aufgeschobenen Substitutionen für die spätere Auswertung zu hinterlegen.

:::info
Für alle `e: Exp` gilt: 
- `evalWithSubst(e) == Num(n)` $\Longleftrightarrow$ `evalWithEnv(e,Map()) == NumV(n)`

- Ist `evalWithSubst(e) == Fun(p,b)` und `evalWithEnv(e) == ClosureV(f,env)`, dann entspricht `Fun(p,b)` dem Ausdruck `f`, in dem für alle Bindungen in `env` Substitution durchgeführt wurde.
:::

Closures sind ein fundamental wichtiges Konzept und tauchen in der Implementation fast aller Programmiersprachen auf.

:::info
Ein **Closure** ist ein Paar, bestehend aus einer Funktionsdefinition und der Umgebung, die bei der Auswertung der Funktionsdefinition aktiv bzw. gültig war.
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

`omega` wird im _Lambda-Kalkül_ notiert als $(\lambda x.(x \; x) \;\; \lambda x.(x \; x))$, der gesamte vordere Ausdruck wird auf den hinteren Ausdruck angewendet, der hintere Ausdruck wird in den Rumpf des vorderen Ausdrucks für $x$ eingesetzt, wodurch wieder der ursprüngliche Ausdruck entsteht.

FAE ist Turing-mächtig, es können also alle Turing-berechenbaren Funktionen repräsentiert werden. Die Sprache entspricht nämlich dem von [Alonzo Church](https://en.wikipedia.org/wiki/Alonzo_Church) eingeführten Turing-mächtigen [Lambda-Kalkül](https://en.wikipedia.org/wiki/Lambda_calculus), erweitert um Zahlen und Addition.

## Lambda-Kalkül
Entfernen wir aus [FAE](#Higher-Order-Funktionen-FAE) den `Num`- und den `Add`-Fall, so erhalten wir eine Sprache, die dem _Lambda-Kalkül_ entspricht:
```scala
sealed abstract class Exp
case class Id(name: String) extends Exp
case class Fun(param: String, body: Exp) extends Exp
case class App(fun: Exp, arg: Exp) extends Exp
```

Der Lambda-Kalkül ist Turing-vollständig, es können darin beliebige Berechnungen ausgedrückt werden. In seiner reinen Form ist es nicht unbedingt eine praktische oder sinnvolle Sprache, dennoch ist der Lambda-Kalkül von einem theoretischen Standpunkt relevant und betrachtenswert.

In dieser Sprache ist jeder Wert eine Funktion (bzw. ein Closure), wodurch keine Typfehler auftreten können.
```scala
abstract class Value
type Env = Map[String, Value]
case class ClosureV(f: Fun, env: Env) extends Value
```

Um mit dieser minimalistischen Sprache sinnvoll arbeiten zu können, sind Kodierungen für verschiedene Datentypen notwendig, dazu kann man bspw. die sogenannten _Church Encodings_ verwenden.

## Church-Kodierungen
**Booleans** werden als ihre "eigene If-Then-Else-Funktion"1 definiert. Darauf basierend lassen sich diverse Bool'sche Operationen definieren:
```scala
val t = Fun("t", Fun("f","t")) // true
val f = Fun("t", Fun("f","f")) // false

val ifTE = Fun("c", Fun("t", Fun("e", App(App("c","t"),"e"))))
val not = Fun("a", App(App("a", f), t)) // if a then False else True
val and = Fun("a", Fun("b", App(App("a", "b"), f))) // if a then b else False
val or = Fun("a", Fun("b", App(App("a", t), "b")))  // if a then True else b
```

**Natürliche Zahlen** können wir als [Peano-Zahlen](https://de.wikipedia.org/wiki/Peano-Axiome) durch eine Nullfunktion und eine Nachfolgerfunktion kodieren. Dabei wird die Zahl $n$ dargestellt durch die $n$-fache Anwendung einer Funktion $s$ auf einen Startwert $z$.
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

Wir können auch mit einer Funktion prüfen, ob eine Zahl 0 ist. Da `zero` als `Fun("s", Fun("z","z"))` kodiert ist, liefert der folgende Ausdruck für 0 $\texttt{True}$, für Zahlen größer 0 die $n$-fache Anwendung von $\lambda x.\texttt{False}$ auf `t`, also $\texttt{False}$.
```scala
val isZero = Fun("n", App(App("a", Fun("x",f)),t))
```

Es können auch eine Vorgängerfunktion `pred` und negative Zahlen mithilfe des sogenannten _Pairing Trick_ kodiert werden, was jedoch deutlich aufwändiger ist.

Auch **Listen** können im Lambda-Kalkül kodiert werden. Die Grundidee ist es, zur Repräsentation die leere Liste $e$ und wiederholte Anwendungen von $c$ (`cons`) zu nutzen. Die Liste $x_1,x_2,...,x_n$ wird also durch $\lambda c. \lambda e. c(x_1, c(... c(x_{n-1}, c(x_n,e))...))$ kodiert. 
```scala
val empty = Fun("c", Fun("e","e"))
val cons =
  Fun("h", Fun("r", Fun("c", Fun("e",
    App(App("c","h"), App(App("r","c"), "e"))))))
```

Die leere Liste `empty` besitzt die gleiche Kodierung wie `zero` und `f`. Bei der `cons`-Operation wird die Restliste `r` durch Applikation auf `c` und `e` "entpackt", durch Anwendung von `c` auf das Element `h` und die "entpackte" Restliste wird das neue Kopfelement vorne an die Restliste angefügt.

```scala
val list123 = App(App(cons,one), App(App(cons,two), App(App(cons,three), empty)))
val listSum = Fun("l", App(App("l",add), zero))
```

Durch Applikationen von `cons` mit jeweils einem Kopfelement und einer Restliste sowie der leeren Liste `empty` lassen sich Listen kodieren, wie es bei `list123` zu sehen ist. Die Applikation einer Liste mit einem `c` und einem `e` entspricht der Listenfaltung, wie sie etwa in Scala oder Racket bzw. Scheme möglich ist:
```scala
List(1,2,3).fold(0)(_+_)
```
```scheme
(foldr + 0 (list 1 2 3))
```

Listen werden also sozusagen durch ihre Fold-Funktion (also durch ihre Faltung mit den Argumenten `c` und `e`) repräsentiert.

Es können auch Listen von Listen kodiert werden, womit es eine Repräsentation von Bäumen im Lambda-Kalkül gibt. Wie wir an unserer eigenen Implementierung sehen können, lassen sich Programme im Lambda-Kalkül sehr gut durch Baumstrukturen darstellen. Dadurch lassen sich auch Lambda-Kalkül-Ausdrücke im Lambda-Kalkül selbst geschickt repräsentieren und es ist möglich, ein Programm im Lambda-Kalkül zu schreiben, das Ausdrücke im Lambda-Kalkül auswertet (vgl. universelle Turing-Maschine).

## Rekursion
Es ist auch möglich, Rekursion im Lambda-Kalkül zu implementieren. Aus dem Programm `omega = (x => x x) (x => x x)` lässt sich das Programm `Y f = (x => f (x x)) (x => f (x x))` konstruieren, mit dem Schleifen kodiert werden können. Das Programm `Y` ist ein sogenannter _Fixpunkt-Kombinator_.

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
Die Auswertungsstrategie eines Interpreters bzw. einer Programmiersprache ist nicht nur eine Frage der Effizienz, sondern hat auch Auswirkungen darauf, welche Programmstrukturen möglich sind. Unendliche Datencontainer wie Streams sind bspw. nur durch _Lazy Evaluation_ überhaupt implementierbar.

In der Sprache Haskell kann man etwa eine rekursive Funktion ohne Abbruchkriterium schreiben, die die Quadratwurzel einer Zahl annähert.

Da Haskell _lazy_ ist, kann eine Funktion ohne Abbruchbedingung definiert und mit verschiedenen Abbruchbedingungen aufgerufen werden, eine Programmstruktur die durch die Auswertungsstrategie der Sprache ermöglicht wird.

Der `evalCBN`-Interpreter erlaubt uns die Kodierung unendlicher Listen in LCFAE durch Church-Encodings (Zusatzmaterial in `08-lcfae.scala`).

## Thunks
Im substitutionsbasierten Interpreter muss nur ein Funktionsaufruf entfernt werden, um die Auswertungsstrategie zu wechseln. Im umgebungsbasierten Interpreter müssen wir dagegen einige Änderungen vornehmen.

Wir müssen bei Funktionsapplikation den Parameter in der Umgebung an das Argument ohne vorherige Auswertung binden. Dabei stoßen wir auf das gleiche Problem, das uns bei Funktionen begegnet ist: Im Argument-Ausdruck, den wir an den Parameternamen binden, müssen noch Bezeichner substituiert werden, wozu die aktuelle Umgebung benötigt wird. Auf diese kann aber zum Zeitpunkt, zu dem das Argument ausgewertet wird, nicht mehr zugegriffen werden.

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
  case class Thunk(exp: Exp, env: Env)
  def delay(e: Exp, env: Env): Thunk = Thunk(e,env)
  def force(t: Thunk): CallByName.Value = {
    println("Forcing evaluation of expression "+t.exp)
    eval(t.exp,t.env)
  }
}
```

Der Typ `Thunk` wird dabei mit den Feldern `exp` und `env` definiert, diese werden durch `delay` mit dem entsprechenden Ausdruck und der aktuellen Umgebung belegt. In `force` werden die Felder ausgelesen und der Ausdruck mit der hinterlegten Umgebung ausgewertet.

## Call-By-Need
Während das Argument unter Call-By-Value immer genau ein Mal ausgewertet wird, findet die Auswertung unter Call-By-Name für jede Verwendung statt. Wird das Argument einer Funktion mehrmals mehrfach weitergereicht, so kann die Anzahl der Aufrufe exponentiell wachsen. Die Auswertungsstrategie kann also in vielen Fällen sehr ineffizient sein. 

Eine bessere Alternative ist die Strategie _Call-By-Name_. Dabei wird nach der ersten Auswertung des Arguments das Ergebnis durch _Caching_ gespeichert, so dass bei mehrfacher Auswertung das abgespeicherte Ergebnis verwendet werden kann und Argumente höchstens ein Mal ausgewertet werden.
```scala
object CallByNeed extends CBN {
  case class MemoThunk(e: Exp, env: Env) {
    var cache: Value = _
  }
  type Thunk = MemoThunk
  def delay(e: Exp, env: Env): MemoThunk = MemoThunk(e,env)
  def force(t: Thunk): CallByNeed.Value = {
    if (t.cache != null)
      println ("Reusing cached value "+t.cache+" for expression "+t.e)
    else {
      println("Forcing evaluation of expression: "+t.e)
      t.cache = eval(t.e, t.env)
    }
    t.cache
  }
}
```

Wir implementieren den Typ `Thunk` wieder als mit den Feldern `exp` und `env`, diesmal aber mit einer zusätzlichen Variablenmitglied `cache`, in dem das Ergebnis der Auswertung hinterlegt werden kann. Bei der Auswertung mit `force` wird überprüft, ob das `cache`-Feld instanziiert wurde. Falls ja, kann das Ergebnis direkt von dort übernommen werden, falls nein, wird der Ausdruck mit der gespeicherten Umgebung ausgewertet und das Ergebnis in `cache` hinterlegt, bevor es ausgegeben wird.

Anhand der `print`-Ausgaben lässt sich erkennen, dass etwa beim folgenden Aufruf drei Mal auf den Cache zugegriffen wird:
```scala
CallByNeed.eval(App(Fun("x", Add(Add("x","x"),Add("x","x"))), Add(2,2,)))
```


# Rekursive Bindings (RCFAE)
In FAE kann Rekursion zwar kodiert werden, im Gegensatz zu [F1-WAE](#First-Order-Funktionen-F1-WAE) ist es aber nicht möglich, dass eine Funktion sich selbst in ihrem Rumpf aufruft:
```scala
val forever = wth("forever", Fun("x", App("forever","x")), App("forever",42))
```
Die Auswertung dieses Ausdrucks würde einen Fehler liefern, da der Bezeichner `"forever"` im Rumpf der Funktion nicht gebunden ist, sondern nur im dritten Ausdruck von `wth`.

Aus diesem Grund ist es auch nicht möglich, mit einem Sprachkonstrukt `If0` rekursive Funktionen mit Abbruchbedingung zu definieren:
```scala
val facAttempt = 
  wth("fac", 
      Fun("n", If0("n", 1, Mul("n", App("fac", Add("n",-1))))), 
      App("fac",4))

// With fac = (n => If (n==0) 1 Else n*fac(n-1)): 
//   fac(4)
```

Um Rekursion in dieser Form erzeugen zu können, brauchen wir ein Konstrukt, mit dem rekursive Bindungen möglich sind. Dazu führen wir das (analog zur Scheme-Funktion benannte) `Letrec`-Konstrukt ein, das die gleiche Form wie `With` bzw. `wth` hat, aber rekursive Bindings ermöglichen soll. Zudem erweitern wir die Sprache um ein `If0`-Konstrukt, um Abbruchbedingungen für rekursive Funktionen geschickt formulieren zu können:
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

Es stellt sich aber die Frage, wie wir im `Letrec`-Fall vorgehen sollen. Zuerst müssen wir `xDef` auswerten, handelt es sich dabei um eine Funktion, so ist das Ergebnis der Auswertung natürlich ein Closure. Die Umgebung im Closure enthält aber keine Bindung für den Funktionsnamen, der ja im Rumpf der Funktion auftritt. 

Selbst wenn man die Umgebung im Closure um eine Bindung für den Funktionsnamen erweitert, würde das nur einen rekursiven Aufruf ermöglichen, dann wäre der Funktionsname im Rumpf bereits wieder nicht gebunden. Für eine unbegrenzte Rekursionstiefe müsste für die Umgebung gelten: `env = Map("fac" -> ClosureV(Fun("n",...), env))`, sie müsste sich also zirkulär selbst referenzieren.

Eine Möglichkeit, um zirkuläre Strukturen zu definieren, ist durch Objektreferenzen (bspw. durch zwei Instanzen, die gegenseitig auf sich verweisen). Hierzu ist Mutation notwendig, es wird die erste Objektinstanz mit Null-Pointer erzeugt, dann die zweite Objektinstanz mit Pointer auf die erste, zuletzt wird der Pointer im ersten Objekt mutiert und auf das zweite gesetzt.

Wir legen eine entsprechende Datenstruktur in einem Objekt `Values` an. Diese besteht aus einem Trait `ValueHolder`, das durch die Klassen `Value` und `ValuePointer` implementiert wird. Instanzen von `Value` sind dabei selbst Werte, Instanzen von `ValuePointer` verweisen auf `Value`-Instanzen. Die `Value`-Subklassen `NumV` und `ClosureV` kennen wir bereits, der Umgebungstyp `Env` ist nun nicht mehr eine Map von `String` nach `Value` sondern von `String` nach `ValueHolder`. Damit diese zirkuläre Definition möglich ist (`ValueHolder -> Value -> ClosureV -> Env -> ValueHolder`), müssen die Definitionen innerhalb eines Objekts liegen.

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

Am Beispiel der `forever`-Funktion geschieht folgendes:
```scala
eval( Letrec("forever", Fun("x",App("forever","x")), App("forever",42)), Map() )
// ~~>
eval( App("forever",42), Map("forever" -> vp) )
// vp.v = ClosureV( Fun("x",App("forever","x")), Map("forever" -> vp) )
```

Bei der Auswertung von `App("forever",42)` wird im `Id`-Fall der Identifier `"forever"` in der Umgebung nachgeschlagen und auf das `value`-Feld des Ergebnisses zugegriffen. Dabei handelt es sich um den oben auskommentierten Closure. Bei der Auswertung des Funktionsrumpfes wird die Umgebung aus dem Closure erweitert, in dieser ist `"forever"` wieder an `vp` gebunden. Bei einem rekursiven Aufruf wird also wieder `vp.value` ausgelesen, etc. 

Damit ist eine unbegrenzte Rekursionstiefe möglich.


# Mutation (BCFAE)
Die Sprache FAE (auch inkl. `Letrec`) ist eine _rein funktionale_ Sprache, also eine Sprache ohne Mutation und Seiteneffekte. In dieser Art von Sprache lassen sich Programme und deren Auswertung besonders leicht nachvollziehen und es liegt _Referential Transparency_ vor.

:::info
**Referential Transparency** bedeutet, dass alle Aufrufe einer Funktion mit dem gleichen Argument überall durch das (identische) Ergebnis des Aufrufs ersetzt werden können, ohne die Bedeutung des Programms zu verändern.
:::

Besitzen Funktionen Seiteneffekte (etwa Print-Befehle oder Mutationen), so ist dies nicht der Fall, denn durch das Ersetzen des Funktionsaufrufs mit dem Ergebnis gehen jegliche Seiteneffekte verloren.

Eine Form von Mutation ist das Mutieren von Variablen, also das Überschreiben des Wertes einer Variable:
```scala
var x = 1
x = 2
```

Eine andere Form ist die mutierbarer Datenstrukturen, bspw. Arrays, in denen einzelne Werte überschrieben werden können. Wir wollen die einfachste denkbare Form einer solchen mutierbaren Datenstruktur unserer Sprache hinzufügen, nämlich eine Datenstruktur mit genau einem Wert, die wir als _Box_ bezeichnen.

## Box-Container
Eine Box entspricht einem Array der Länge 1, ist also ein Datencontainer für genau einen Wert. Um Boxes zu implementieren, führen wir die folgenden Sprachkonstrukte ein:
```scala
case class NewBox(e: Exp) extends Exp
case class SetBox(b: Exp, e: Exp) extends Exp
case class OpenBox(b: Exp) extends Exp
case class Seq(e1: Exp, e2: Exp) extends Exp
```

Wir müssen Boxen instanziieren, beschreiben und auslesen bzw. dereferenzieren können, zudem brauchen wir eine Möglichkeit, um zu Sequenzieren, also zwei Ausdrücke nacheinander auszuwerten (damit ein Ausdruck neben einer Mutation auch eine Berechnung durchführen kann). Da der Wert einer Box mutiert werden kann, spielt die Auswertungsreihenfolge von Unterausdrücken nun eine entscheidende Rolle.

Wir implementieren den `Box`-Container aus pädagogischen Gründen nicht durch Mutation in der Meta-Sprache, sondern bleiben weiterhin bei einem funktionalen Interpreter.

```scala
val ex1 = wth("b", NewBox(0), Seq( SetBox("b", Add(1,OpenBox("b"))), OpenBox("b")))
/* Should evaluate to 1.
With b = NewBox(0):
  SetBox(b <- 1+OpenBox(b));
    OpenBox(b)
 */

```

Die Implementierung von `Seq` stellt uns vor eine Herausforderung, die Reihenfolge der rekursiven `eval`-Aufrufe in unserem Interpreter spielt nämlich keine Rolle, da diese Funktionsaufrufe keine Effekte haben (und auch nicht haben sollen). Wir müssen also unseren Interpreter abändern, so dass nach der Auswertung sowohl das Ergebnis, als auch durchgeführte Mutationen zurückgegeben werden, damit wir beim Auswerten des zweiten Programmabschnitts die Effekte des ersten Programmabschnitts berücksichtigen können.

Hierzu ist es aber nicht ausreichend, `(Value,Env)` als Rückgabetyp zu wählen, wie das folgenden Beispiel zeigt:
```scala
val ex2 = wth("a", NewBox(1), 
            wth("f", Fun("x", Add("x", OpenBox("a"))),
              Seq(SetBox("a",2), App("f",5))))
/* Should evaluate to 7.
With b = NewBox(1):
  With f = (x => x+OpenBox(b):
    SetBox(a, 2);
    f(5)
 */
```

Bei der Auswertung von `f` wird die Umgebung aus dem zu `f` gehörigen Closure verwendet. In dieser Umgebung steht in der an `"a"` gebundenen Box der Wert `1`, nicht `2`. Environments dienen zur Umsetzung von lexikalischem Scoping, sie werden entsprechend der Programmstruktur rekursiv in Unterausdrücke weitergereicht. Für Mutation ist aber die Auswertungsreihenfolge und nicht die syntaktische Struktur des Programms entscheidend, insofern sind Environments für die Implementation dieses neuen Features ungeeignet. 

Stattdessen benötigen wir eine zweite Datenstruktur, die wir als zusätzliches Argument bei der Auswertung übergeben und in evtl. modifizierter Form nach der Auswertungs ausgeben. Die neue Datenstruktur besitzt einen ganz anderen Datenfluss als die Umgebung, sie wird von Auswertungsposition zu Auswertungsposition gereicht und nicht wie die Umgebung im AST immer nur nach unten weitergegeben.

## Store und Adressen
Wir verwenden wieder unsere alten Definitionen von `Value` und `Env` und führen die Typen `Address` und `Store` ein. Zusätzlich erweitern wir `Value` um den Fall `AddressV`:
```scala
sealed abstract class Value
type Env = Map[String,Value]
case class NumV(n: Int) extends Value
case class ClosureV(f: Fun, env: Env) extends Value

type Address = Int
case class AddressV(a: Address) extends Value
type Store = Map[Address,Value]
```

Adressen sind Integers und dienen als Referenzen ("Pointer") für Box-Instanzen. In der `Store`-Map werden die aktuellen Werte der Box-Instanzen hinterlegt. Um Identifier an Boxen binden zu können, müssen wir Boxen als `Value` repräsentieren können, zu diesem Zweck dient `AddressV`.

Um neue Adressen zu erhalten, inkrementieren wir einfach die bisher höchste Adresse um 1. Um das Entfernen alter Referenzen und die Limitierungen dieses Adressen-Systems kümmern wir uns zum jetzigen Zeitpunkt nicht.
```scala
var address = 0
def nextAddress : Address = {
  address += 1
  address
}
```
Hier verwendet unsere Implementation doch Mutation in der Meta-Sprache, aber nur um diese Hilfsfunktion zu vereinfachen. Alternativ wäre eine Funktion `freshAddress` denkbar, die eine neue ungenutzte Adresse erzeugt (vgl. `freshName` [hier](#Capture-Avoiding-Substitution))

## Interpreter
Im Interpreter ändert sich in allen Fällen deutlich, die Implementation wird im Allgemeinen aufwändiger:
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
  case App(f,a) => eval(f,env,s) match {
    case (ClosureV(Fun(p,b),cEnv),s1) => eval(a,env,s1) match {
      case (v,s2) => eval(b, cEnv+(p -> v), s2)
    }
    case _ => sys.error("Can only apply functions")
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

Durch die Auswertung des linken Teilausdrucks (mit aktueller Umgebung und aktuellem Store) erhalten wir ein Tupel aus `Value` und `Store`, diesen neuen `Store` verwenden wir dann bei der Auswertung des rechten Teilausdrucks, so dass potentielle Mutierungen im linken Teilausdruck bei der Auswertung des rechten Teilausdrucks berücksichtigt werden. Die Auswertung des rechten Teilausdrucks liefert wieder einen Wert und einen Store, wir geben die Summe der Zahlen und den neuesten Store als Ergebnis aus.

Auch im `App`-Fall müssen wir den Store erst in den linken Unterausdruck und dann (potentiell modifiziert) in den rechten Ausdruck reichen, der Store, der bei der Auswertung des Arguments ausgegeben wird, verwenden wir bei der Auswertung des Funktionsrumpfs.

Im `Seq`-Fall werten wir zuerst den linken Ausdruck aus und greifen mit `._2` auf den Store aus dem Ergebnis zu. Diesen nutzen wir dann bei der Auswertung des rechten Ausdrucks. Das Ergebnis des linken Teilausdrucks wird also ignoriert, es wird das Ergebnis des rechten Teilausdrucks ausgegeben.

Um eine neue Box-Instanz zu erzeugen, werten wir zuerst den Ausdruck aus, der in der Box stehen soll. Wir erhalten einen Wert `v` und einen Store `s1`, erzeugen mit `nextAddress` eine neue Adresse und geben ein Tupel aus der neuen Adresse und `s1`, erweitert um eine Bindung der neuen Adresse an `v`, aus. Im `SetBox`-Fall muss zusätzlich der Ausdruck an der ersten Stelle ausgewertet werden, um die Adresse der Box-Instanz zu erhalten und den Store mit dem neuen Wert zu aktualisieren. Im `OpenBox`-Fall wird auch erst die Adresse bestimmt, anschließend wird der an die Adresse gebundene Wert und der aktuelle Store ausgegeben.

Beim Auslesen einer Box-Instanz wird also erst in der Umgebung der Bezeichner nachgeschlagen, was einen `AddressV`-Wert liefern sollte, anschließend wird im Store nachgeschlagen, auf welchen Wert diese Adresse verweist. 


# Speichermanagement
Die Funktion `nextAddress`, mit der wir ungenutzte Adressen für neue Boxen erzeugen, inkrementiert einfach die Variable `address` immer weiter. Der Wert von `address` wird nach der Auswertung nicht zurückgesetzt. Während der Auswertung wird auch nicht geprüft, welche Einträge im Store noch benötigt werden und ob Adressen und die an sie gebundenen Werte entfernt werden können.

Eine Möglichkeit, nicht mehr benötigte Einträge zu entfernen, wäre ein neues Sprachkonstrukt, etwa `RemoveBox`. Damit könnte der Programmierer Box-Instanzen verwerfen, die nicht mehr benötigt werden. Wird aber ein Identifier an eine Box-Instanz gebunden und diese Box-Instanz gelöscht, so verweist der Identifier weiterhin auf eine Adresse, für die es im Store aber keinen Eintrag mehr oder sogar einen anderen, neuen Eintrag gibt.

Unter anderem durch solche _Dangling Pointers_ ist Programmieren mit manuellem Speichermanagement fehleranfällig, auch wenn dadurch performantere Programme möglich sind. Aus diesem Grund verwenden viele Programmiersprachen automatisches Speichermanagement in Form von _Garbage Collection_.

## Garbage Collection
Garbage Collection beruht auf der Tatsache, dass algorithmisch bestimmt bzw. approximiert werden kann, welche Speicherinhalte in der weiteren Auswertung noch benötigt werden.

Ideal wäre ein Garbage-Collection-Algorithmus, der folgendes erfüllt:

:::info
**"Perfekte" Garbage Collection:** Wenn die Adresse $a$ im Store $s$ in der weiteren Berechnung nicht mehr benötigt wird, so wird der Eintrag für $a$ aus $s$ entfernt.
:::

Die Fragestellung, ob ein Store-Eintrag in der weiteren Berechnung noch benötigt wird, ist jedoch unentscheidbar, was aus der Unentscheidbarkeit des Halteproblems und dem Satz von Rice folgt. Wird bspw. eine Funktion $f$ aufgerufen und danach auf eine Adresse zugegriffen, so wird die Adresse nur benötigt, wenn $f$ terminiert. Für perfekte Garbage Collection müsste also das Halteproblem entscheidbar sein.

Es kann jedoch die Menge der noch benötigten Adressen approximiert werden. Approximinieren bedeutet dabei, das es Adressen gibt, für die keine Entscheidung möglich ist oder die falsch eingeordnet werden. Garbage Collection wird dabei so gestaltet, dass nur eine Art von Fehler geschieht, nämlich dass Daten unnötig/fälschlicherweise im Speicher gehalten werden aber nie fälschlicherweise verworfen werden (_Soundness_). 

_Reachability_ von Speichereinträgen hat sich als eine geeignete Approximation für die Zwecke von Garbage Collection herausgestellt und wird in vielen Algorithmen benutzt, um nicht mehr benötigte Einträge zu bestimmen.

:::info
**Erreichbarkeit/Reachability:** Eine Adresse ist _erreichbar_, wenn sie sich in der aktuellen Umgebung (inkl. Unterumgebungen in Closures, usw.) befindet, oder wenn es einen Pfad von Verweisen aus der aktuellen Umgebung zu der Adresse gibt. 
:::

Garbage Collection entspricht also dem Erreichbarkeitsproblem in einem gerichteten Graphen. Voraussetzung ist dabei, dass alle nicht erreichbaren Adressen im Rest der Berechnung nicht benötigt werden. Das wäre bspw. nicht der Fall, wenn durch Pointer-Arithmetik auf beliebige Adressen zugegriffen werden kann.

Da unsere Auswertungsfunktion rekursiv ist, reicht es nicht, nur die aktuelle Umgebung zu betrachten. Es muss für jede Instanz der `eval`-Funktion auf dem Call-Stack die zugehörige Umgebung berücksichtigt werden, da beim Aufstieg aus den rekursiven Aufrufen wieder die auf dem Stack abgelegten Umgebungen verwendet werden. 

## Mark and Sweep
Die meisten einfachen Garbage-Collection-Algorithmen auf Basis von Reachability bestehen aus den zwei Phasen _Mark_ und _Sweep_. Im ersten Schritt werden alle Adressen markiert, die noch benötigt werden, im zweiten Schritt werden dann alle nicht markierten Adressen entfernt.

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
  
  store.filter{ case (a,_) => marked(a) } // sweep

}
```

Die Funktion `allAddrInEnv` sammelt alle erreichbaren Adressen in einer Umgebung, indem `allAddrInVal` auf alle Werte in der Umgebung aufgerufen wird und die entstehenden Mengen alle vereinigt werden. Die Funktion `mark` erhält eine Adressmenge `seed` und liefert die Menge aller Adressen, die von den Adressen in `seed` aus erreicht werden können. Werden dabei keine neuen Adressen gefunden, so wird die Menge `seed` ausgegeben. Ansonsten wird `mark` rekursiv aufgerufen, dabei wird die Menge um die neu gefundenen Adressen erweitert. Sie wird also schrittweise erweitert, bis keine neuen Adressen mehr gefunden werden. `gc` bestimmt erst die Menge der markierten Adressen und filtert dann den Store, so dass unmarkierte Adressen entfernt werden.

Das folgende Beispiel zeigt die Funktionsweise des Algorithmus, `gc` wird mit einer Umgebung aufgerufen, in der auf der rechten Seite die Adresse `5` vorkommt. An der Adresse `5` steht im Store eine Closure, in dem wiederum die Adresse `3` auftritt, an der Adresse `3` wird auf die Adresse `1` verwiesen. Die Adressen `2` und `5` sind nicht erreichbar und sind dementsprechend im Ergebnis-Store nicht mehr vorhanden.
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
Durch wiederholtes Anlegen und Entfernen von Speichereinträgen kann es zu einer starken Fragmentierung des Speichers kommen, die belegten Speicherzellen sind dann evtl. stark verteilt und der Speicher ist "lückenhaft" befüllt.

Diese Fragmentierung erschwert zum einen die Speicherzuweisung, da größere "Datenblöcke" evtl. nicht am Stück gespeichert werden können und zerlegt werden müssen, zum anderen verschlechtert sich die Performanz, da die Speichernutzung weniger effizient wird (freier Speicher ist verteilt und kann dadurch evtl. nicht genutzt werden, es müssen mehr Adressen im Speicher gehalten werden, zusammengehörige Daten werden nicht automatisch gemeinsam in den Cache geladen).

Um Fragmentierung zu reduzieren, müssen bei der Garbage Collection Speichereinträge verschoben ("zusammengerückt") werden, sodass die Speicherbelegung möglichst dicht bzw. kompakt bleibt. Hierbei spricht man von _Moving_ Garbage Collection. Dabei werden nicht nur die Daten verschoben, sondern es müssen auch alle Referenzen mit der neuen Speicheradresse aktualisiert werden.

Bei [Mark and Sweep](#Mark-and-Sweep) werden die Daten nicht verschoben, der Algorithmus ist eine Form von _Non-Moving_ Garbage Collection. Er führt allgemein über die Laufzeit hinweg zu zunehmender Fragmentierung.

Ein Beispiel für Moving Garbage Collection ist _Semi-Space Garbage Collection_. Dabei wird der Speicher in zwei Hälften geteilt, wobei während der Allokation nur eine Hälfte des Speichers verwendet wird. Ist diese voll, so wird Garbage Collection durchgeführt: Die noch benötigten Einträge werden markiert, anschließend werden alle markierten Einträge in die freie Speicherhälfte kopiert, wodurch der Speicher wieder dicht belegt wird. Zuletzt werden alle Einträge in der vollen Speicherhälfte gelöscht. Beim nächsten GC-Zyklus wird das Verfahren mit umgekehrten Rollen wiederholt. 

**Vorteil** ist, dass bei jedem GC-Zyklus der gesamte Speicher defragmentiert wird, das Problem der steigenden Fragmentierung über Zeit ist also behoben. 
**Nachteile** sind die hinzukommenden Kopieroperationen und größere Anzahl an Löschoperationen, die Aktualisierung der Referenzen und die dazu notwendigen Suchoperationen, sowie die (im Worst Case) Halbierung des verfügbaren Speicherplatzes.

## Weitere Begriffe
- **Generational GC:** Es kann empirisch belegt werden, dass in den meisten Anwendungen die Objekte, die bei einem GC-Zyklus dereferenziert werden können, tendenziell sehr "jung" sind, also erst vor kurzer Zeit angelegt wurden. Bei Objekten, die sich schon sehr lange im Speicher befinden, werden viel wahrscheinlicher noch benötigt als Objekte, die erst kürzlich angelegt wurden.
&nbsp;
Bei _Generational Garbage Collection_ macht man sich diese Tatsache zunutze, indem die Objekte nach ihrem Alter aufgeteilt werden und im Speicherbereich für jungen Objekte öfter Garbage Collection durchgeführt wird als im Speicherbereich für alte Objekte. Somit kann Speicherplatz effizienter freigegeben werden, da gezielt die Objekte betrachtet werden, die am ehesten dereferenziert werden können. 

- **"Stop the World"-Phänomen:** Während Garbage Collection durchgeführt wird, kann eine Anwendung i.A. nicht weiterlaufen, denn der GC-Algorithmus wäre nicht mehr sicher, wenn während der Ausführung des GC-Algorithmus weiter Adressen angelegt und Referenzen geändert werden. Stattdessen muss der Anwendungsprozess aufgeschoben werden, bis der GC-Zyklus vollendet ist. 
&nbsp;
In den meisten Fällen kann dies unbemerkt geschehen, aber bei interaktiven Programmen und Echtzeit-Anwendungen wird die Garbage Collection und das damit verbundene Aussetzen des Programms evtl. durch Ruckeln oder kurzes "Hängen" bemerkbar. Im besten Fall ist das für einen Nutzer leicht störend, im schlimmsten Fall hat die verzögerte Reaktion aber weitreichende Folgen, weshalb automatisches Speichermanagement für Programme mit extrem hohen Ansprüchen an die Reaktionsfähigkeit und Zuverlässigkeit ungeeignet sein kann. 

- **Reference Counting:** Eine andere Form des automatischen Speichermanagements, die nicht auf Erreichbarkeit von Objektinstanzen beruht, ist _Reference Counting_. Dabei wird zusammen mit jeder Objektinstanz ein Feld angelegt, in dem die Anzahl der Referenzen auf das Objekt gehalten wird. Ist diese Anzahl 0, so kann das Objekt dereferenziert werden. Bei jeder Änderung der Referenzen müssen die Felder in den betroffenen Objekten aktualisiert werden. Im Gegensatz zu Garbage Collection muss die Anwendung nicht mehr unterbrochen werden, Objekte können gelöscht werden, sobald ihr Counter 0 beträgt.
&nbsp;
Gibt es jedoch Referenzzyklen, so werden Objekte, die evtl. nicht mehr erreichbar sind, dennoch im Speicher gehalten. Deshalb wird das Verfahren typischerweise mit Zyklendetektion kombiniert, damit solche Strukturen erkannt und die entsprechenden Objekte dereferenziert werden.


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

Statt einer Umgebung verwendet der Interpreter nun eine Liste aller im Call-Stack auftretenden Umgebungen als Parameter. Der Kopf der Liste ist die aktive Umgebung des aktuellen Funktionsaufrufs, die weiteren Elemente sind die Umgebungen der "darüberliegenden" Aufrufe in der Rekursionsstruktur, als die auf dem Call-Stack abgelegten Umgebungen. Im `App`-Fall wird die Umgebungsliste erweitert, indem vorne an die Liste eine neue Umgebung angehängt wird, die zusätzlich die Bindung des Parameters enthält. Im `Id`-Fall wird der Bezeichner in der obersten/ersten Umgebung im Stack nachgeschlagen.

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

Der Store ist hierbei durch ein Array implementiert, um eine freie Adresse zu finden wird die mutierbare Variable `nextFreeAddr` verwendet und nach jeder neuen Zuweisung inkrementiert. Die Größe `size` wird bei der Instanziierung gewählt und legt fest, wie groß das Array ist, also wie viele Adressen es gibt. Sind alle Indices des Array belegt worden, so ist der Speicher voll und es wird eine Fehlermeldung ausgegeben. Es wird keinerlei Garbage Collection betrieben, um Ressourcen freizugeben. 

Die `eval`-Aufrufe im folgenden Programm verursachen also eine Fehlermeldung, wenn bei der Evaluation von `ex1` mindestens eine Box angelegt wird:
```scala
val store = new StoreNoGC(2)
val stack = List[Env]()
eval(ex1,stack,store); eval(ex1,stack,store); eval(ex1,stack,store)
```

Auch ein Testprogramm, in dem mehr als zwei Boxen instanziiert werden, würde einen Fehler liefern.

## Mit GC
Wir implementieren nun die abstrakte `Store`-Klasse mit "Mark & Sweep"-Garbage-Collection. Der Store wird dabei wieder mit einer Größe `size` instanziiert, die die Anzahl der Adressen bestimmt. Wir ergänzen eine Variable `free`, in der die Anzahl ungenutzter Adressen gehalten wird. Gibt es bei der Speicherallokation keine freien Adressen mehr, so wird Garbage Collection betrieben. Ist auch danach keine Adresse frei, so wird eine Fehlermeldung ausgegeben. Der verwendete "Mark & Sweep"-Algorithmus ähnelt dem im [vorherigen Kapitel](#Mark-and-Sweep) aufgeführten stark:

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
    env.values.map(allAddrInVal).fold(Set())(_++_)
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




