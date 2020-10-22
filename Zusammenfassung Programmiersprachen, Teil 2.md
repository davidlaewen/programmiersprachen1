---
title: Zusammenfassung Programmiersprachen, Teil 2
description: Programmiersprachen 1, SoSe 2020, Klauso3
langs: de

---

# Inhaltsverzeichnis

[TOC]


# Objekt-Algebren
_Objekt-Algebren_ (_Object Algebras_) sind eine Abstraktion, die eng mit [algebraischen Datentypen](https://en.wikipedia.org/wiki/Algebraic_data_type) und [Church-Kodierungen](#Church-Kodierungen) verwandt ist. Zudem haben sie einige Gemeinsamkeiten mit dem [Visitor Pattern](https://pad.its-amazing.de/programmiersprachen1teil1#Abstraktion-durch-Visitor).

Sie sind ein mächtiger und teilweise auch effizienter Mechanismus zur Modularisierung von Programmen und ein sehr junges Forschungsgebiet, zu dem es erst seit etwa 2012 Veröffentlichungen gibt.

## Binärbäume im Objekt-Algebra-Stil
Betrachten wir zuerst eine Implementation von Binärbäumen im Visitor-Stil (völlig analog zum [Interpreter im Visitor-Stil](#Abstraktion-durch-Visitor)):
```scala
sealed abstract class BTree
case class Node(l: BTree, r: BTree) extends BTree
case class Leaf(n: Int) extends BTree

case class Visitor[T](node: (T,T) => T, leaf: Int => T)

def foldExp[T](v: Visitor[T], b: BTree) : T = b match {
  case Node(l,r) => v.node(foldExp(v,l), foldExp(v,r))
  case Leaf(n)   => v.leaf(n)
}

val sumVisitor = new Visitor[Int]((l,r) => l+r, n => n)

assert(foldExp(sumVisitor, Node(Leaf(1),Leaf(2))) == 3)
```

Im Objekt-Algebra-Stil wird der Datentyp nicht durch eine abstrakte Klasse mit verschiedenen _Cases_, sondern durch die Funktionen der Faltungsoperation definiert, die Daten werden also durch ihre eigene Faltung repräsentiert:
```scala
trait BTreeInt[T] {
  def node(l: T, r: T) : T
  def leaf(n: Int) : T
}

def ex1[T](semantics: BTreeInt[T]) : T = {
  import semantics._
  node(leaf(1),leaf(2))
}

object TreeSum extends BTreeInt[Int] {
  def node(l: Int, r: Int) = l+r
  def leaf(n: Int) = n
}

assert(ex1(TreeSum) == 3)
```

Dadurch muss zur Durchführung einer Faltung nur eine Instanziierung von `BTreeInt` (hier etwa `TreeSum`) an einen Wert des Datentyps (hier etwa `ex1`) überreicht werden, in der die Faltungsoperation für beide Fälle konkretisiert sind.e

**Eigenschaften des Objekt-Algebra-Stils:**
- Jeder Konstruktor des Datentyps (hier `Node` und `Leaf`) wird zu einer Funktion in der abstrakten "Signaturklasse" mit Typparameter `T` (hier `BTreeInt[T]`), wobei rekursive Vorkommen des Datentyps durch `T` ersetzt werden.

- Konkrete Werte des Datentyps sind als Funktion mit Typparameter `T` definiert, wobei die Eingabe eine Instanz der Signaturklasse und die Ausgabe vom Typ `T` ist.

- Konkrete Faltungen werden zu Instanziierungen der Signaturklasse (hier `TreeSum`), in der die Faltungsfunktionen für die verschiedenen Fälle definiert werden.

Die Argumente der Faltungsfunktion werden also in einem `Trait` zusammengefasst und können als Objekt überreicht werden. Dadurch lässt sich der Datentyp durch Vererbung erweitern (etwa um Blätter, die Strings enthalten):
```scala
trait BTreeMixed[T] extends BTreeInt[T] {
  def leaf(s: String) : T
}

def ex2[T](semantics: BTreeMixed[T]) : T = {
  import semantics._
  node(leaf(1),node("a"))
}
```

## Peano-Zahlen im Objekt-Algebra-Stil
Wir können die Church-Kodierungen für Zahlen folgendermaßen in Scala nachbilden, wobei Zahlen Instanzen von `Num` sind und (entsprechend der Church-Kodierung) aus ihrer eigenen Faltungsfunktion bestehen:
```scala
trait Num {
  def fold[T](s: T => T, z: T) : T
}

case object Zero extends Num {
  def fold[T](s: T => T, z: T) : T = z
}

case object One extends Num {
  def fold[T](s: T => T, z: T) : T = s(z)
}

def succ(n: Num) : Num = {
  new Num {
    def fold[T](s: T => T, z: T) : T = s(n.fold(s,z))
  }
}
```

Bei der Implementierung im Objekt-Algebra-Stil werden die Funktionen der Faltung (also Parameter von `fold`) wieder zu einem Objekt zusammengefasst:
```scala
trait NumSig[T] {
  def s(p: T) : T
  def z: T
}

trait Num { def apply[T](x: NumSig[T]) : T }
```

Eine Objekt-Algebra ist eine Klasse, die ein generisches _Abstract Factory Interface_ implementiert, das im wissenschaftlichen Gebiet der _universellen Algebra_ als _algebraische Signatur_ bezeichnet wird. Die obige Implementation im Objekt-Algebra-Stil ist eine _algebraische Struktur_ mit den Operationen `s` und `z`. Der Typ der Operationen (hier `NumSig`) wird als _Signatur_ oder _Funktor_ bezeichnet, bei `Num` handelt es sich um eine _(Funktor-)Algebra_.

:::info
Eine **algebraische Struktur** besteht gewöhnlich aus einer nichtleeren Menge (_Grundmenge_ oder _Trägermenge_) und einer Familie von _inneren Verknüpfungen_ (_Grundoperationen_) auf der Menge. Ein Beispiel für eine einfache algebraische Struktur ist etwa das _Monoid_.

Ein **Monoid** ist eine algebraische Struktur bestehend aus einer Menge $M$, einer Abbildung $\odot: M \times M \to M$ und einem neutralen Element $e \in M$, so dass $\forall a \in M$ gilt: $e \odot a = a \odot e = a$. Außerdem gilt $\forall a,b,c \in M: (a \odot b) \odot c = a \odot (b \odot c)$ (_Assoziativität_).
:::

Wir implementieren Zahlen und Addition im Objekt-Algebra-Stil folgendermaßen:
```scala
val zero: Num = new Num { def apply[T](x: NumSig[T]): T = x.z }
val one: Num = new Num { def apply[T](x: NumSig[T]): T = x.s(x.z) }
val two: Num = new Num { def apply[T](x: NumSig[T]) : T = x.s(one.apply(x))}

def plus(a: Num, b: Num) : Num = new Num {
  def apply[T](x: NumSig[T]) : T = a.apply(new NumSig[T] {
    def s(p: T): T = x.s(p)
    def z: T = b.apply(x)
  })
}
```

Dieser Stil ermöglicht verschiedene konkrete Implementierungen des Interfaces, bei denen die Argumente für die Faltung übergeben werden, die die `Num`-Objekte in der `apply`-Funktion noch erwarten.
```scala
object NumAlg extends NumSig[Int] {
  def s(x: Int) : Int = x+1
  def z = 0
}

assert(two(NumAlg) == 2)
assert(plus(one,two)(NumAlg) == 3)
```
Wir können bspw. das Objekt `NumAlg` überreichen, um die Church-kodierten Zahlen in Scala-Integer umzuwandeln. Dabei wird implizit `apply` mit der übergebenen `NumSig`-Instanziierung aufgerufen. 

In diesem Fall bietet der Objekt-Algebra-Stil keine speziellen Vorteile, anders ist es aber bei unserem Interpreter.

## Interpreter im Objekt-Algebra-Stil
Der Objekt-Algebra-Stil erzwingt Kompositionalität, deshalb wandeln wir unsere kompositionale Implementation von FAE, in der Closures durch Metainterpretation umgesetzt sind, zur Umwandlung.
```scala
trait Exp[T] {
  implicit def num(n: Int) : T
  implicit def id(name: String) : T
  def add(l: T, r: T) : T
  def fun(param: String, body: T) : T
  def app(fun: T, arg: T) : T
  def wth(x: String, xDef: T, body: T) : T = app(fun(x,body),xDef)
}

sealed abstract class Value
type Env = Map[String,Value]
case class ClosureV(f: Value => Value) extends Value
case class NumV(n: Int) extends Value

trait eval extends Exp[Env => Value] {
  def id(x: String) : Env => Value = env => env(x)
  def fun(p: String, b: Env => Value) : Env => Value =
    env => ClosureV(v => b(env+(p -> v)))
  def app(fun: Env => Value, arg: Env => Value) : Env => Value =
    env => fun(env) match {
      case ClosureV(f) => f(arg(env))
      case _ => sys.error("Can only apply functions")
    }
  def num(n: Int) : Env => Value = _ => NumV(n)
  def add(l: Env => Value, r: Env => Value) : Env => Value =
    env => (l(env),r(env)) match {
      case (NumV(a),NumV(b)) => NumV(a+b)
      case _ => sys.error("Can only add numbers")
    }
}

object eval extends eval

def test[T](semantics: Exp[T]) = {
  import semantics._
  app(app(fun("x",fun("y",add("x","y"))),5),3)
}

assert(test(eval)(Map()) == NumV(8))
```

Dieser Stil erlaubt vollständige Modularität, so dass Sprachkonstrukte nach Bedarf hinzugefügt werden können. Wir können die Sprache etwa folgendermaßen um Multiplikation erweitern:
```scala
trait ExpWithMul[T] extends Exp[T] {
  def mul(l: T, r: T) : T
}

object evalWithMul extends eval with ExpWithMul[Env => Value] {
  def mul(l: Env => Value, r: Env => Value) : Env => Value =
    env => (l(env),r(env)) match {
      case (NumV(a),NumV(b)) => NumV(a*b)
      case _ => sys.error("Can only multiply numbers")
    }
}
def test2[T](semantics: ExpWithMul[T]) = {
  import semantics._
  app(app(fun("x",fun("y",mul("x","y"))),5),3)
}

assert(test2(evalWithMul)(Map()) == NumV(15))
```

## Expression Problem
Bei unserem Interpreter gibt es zwei Arten von Erweiterung: Zum einen gibt es die Erweiterung um zusätzliche Funktionen neben `eval` (bspw. `print` oder `countNodes`), zum anderen gibt es die Erweiterung um zusätzliche Sprachkonstrukte. 
- Unsere bisherige (funktionale) Implementation mit Pattern Matching erlaubt die modulare Erweiterung um neue Funktionen -- es können neben `eval` weitere Funktion angelegt werden, die auf Expressions operieren. Die Erweiterung um neue Sprachkonstrukte ist aber nicht modular möglich, denn die Funktionen können nach ihrer Definition nicht mehr um zusätzliche Fälle erweitert werden.
```scala
sealed trait Exp
case class Num(n: Int) extends Exp
case class Add(l: Exp, r: Exp) extends Exp

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Add(l,r) => eval(l) + eval(r)
}

// ...
```

- Bei einer objektorientierten Implementierung können Funktionen nicht nachträglich hinzugefügt werden, da diese in jedem Konstruktor definiert werden müssen. Modulare Ergänzung neuer Sprachkonstrukte ist hingegen gut möglich, die abstrakte Oberklasse kann nachträglich erweitert werden. 
```scala
sealed abstract class Exp {
  def eval() : Int
}

case class Num(n: Int) extends Exp {
  def eval() = n
}

case class Add(l: Exp, r: Exp) extends Exp {
  def eval() = l.eval + r.eval
}

// ...
```

Der große Vorteil des Objekt-Algebra-Stils ist die modulare Erweiterbarkeit in beiden Dimensionen. Es können sowohl neue Sprachkonstrukte modular ergänzt, als auch neue Funktionen neben `eval` hinzugefügt werden. Sie stellen im Allgemeinen eine Lösung für das sogenannte _Expression Problem_ dar.

:::info
Das **Expression Problem** beschreibt die Suche nach einer Datenabstraktion, bei der sowohl neue Datenvarianten (Cases), als auch neue Funktionen, die auf dem Datentyp operieren, ergänzt werden können, ohne dass bisheriger Code modifiziert werden muss und wobei Typsicherheit gewährt ist ([Wikipedia-Artikel](https://en.wikipedia.org/wiki/Expression_problem)).

Bei einem funktionalen Ansatz ist typischerweise die Erweiterung mit Operationen gut unterstützt, bei einem objektorientierten Ansatz hingegen die Erweiterung mit neuen Datenvarianten.
:::


# Webprogrammierung mit Continuations
Angenommen, man will eine interaktive Webanwendung über mehrere Webseiten hinweg programmieren, so wird man vor eine Herausforderung gestellt: Das Webprotokoll HTTP ist zustandslos, d.h. ein Programm im Web terminiert nach jeder Anfrage. Anfragen sind unabhängig voneinander und es ist kein Zugriff auf vorherige Anfragen und dabei übermittelte Daten möglich. Betrachten wir etwa die interaktive Funktion `progSimple` im folgenden Code:
```scala
import scala.io.StdIn.readLine

def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def progSimple() : Unit = {
  println(inputNumber("First number:") + inputNumber("Second number:"))
} 
```

Hier finden nacheinander zwei Eingaben durch den Nutzer statt, wobei das Ergebnis aus beiden Werten berechnet wird. Würde man diese Funktion im Web umsetzen wollen, so dass der Nutzer auf zwei verschiedenen Seiten die Werte eingibt und absendet und auf einer dritten Seite das Ergebnis angezeigt bekommt, dann wäre aufgrund der Zustandslosigkeit von HTTP ein spezieller Programmierstil notwendig. 

Der Ablauf zerfällt dabei in die folgenden Teilprogramme:
- **Teilprogramm $a$** zeigt das Formular für die erste Zahl an.
- **Teilprogramm $b$** konsumiert die Zahl aus dem ersten Formular und generiert das Formular für die zweite Zahl.
- **Teilprogramm $c$** konsumiert die Daten aus dem zweiten Formular, berechnet die Ausgabe und erzeugt die Seite mit dem Ergebnis.

Nun stellt sich die Frage, wie im zustandslosen Protokoll das Teilprogramm $c$ auf die in $a$ eingegebenen Daten zugreifen kann. Hierzu muss der eingegebene Wert über $b$ weitergereicht werden, etwa als verstecktes Formularfeld in HTML oder als Parameter in der URL. 

Wir können die Zustandslosigkeit mit dem Rückgabetyp `Nothing` modellieren, dabei brechen wir alle Funktionen mit einem Fehler ab, so dass sie nicht zurückkehren.
```scala
def webDisplay(s: String) : Nothing = {
  println(s)
  sys.error("Program terminated")
}

def webRead(prompt: String, continue: String) : Nothing = {
  println(prompt)
  println("Send input to "+continue)
  sys.error("Program terminated")
}

def progA = webRead("First number:", "progB")
def progB(n: Int) = webRead("First number was "+n+"\nSecond number:", "progC")
def progC(n1: Int, n2: Int) = webDisplay("Sum of "+n1+" and "+n2+" is "+(n1+n2))
```

Hier wird die Weitergabe der Daten von einem Teilprogramm zum nächsten nur durch die Ausgaben und Eingaben in der Konsole (also händisch durch den Nutzer) modelliert, d.h. es wird bspw. erst `progA` mit `2` aufgerufen, dann `progB` mit `3` und zuletzt `progC` mit `2` und `3`. Dadurch ist der Nutzen der durchgeführten Programmtransformation noch nicht sonderlich klar erkennbar. Wie könnten ausdrücken wenn wir die noch bevorstehenden Schritte zum Zeitpunkt des letzten Programmabbruchs zur Verfügung hätten.

## Implementierung mit Continuations
Es können aber auch die jeweils noch notwendigen Schritte als _Continuation_ repräsentiert werden, im Fall von `progA` muss etwa noch die zweite Zahl eingelesen werden, dann müssen beide Zahlen addiert und das Ergebnis ausgegeben werden. In der ursprünglichen Variante des Programms
```scala
def progSimple() : Unit = {
  println(inputNumber("First number:") + inputNumber("Second number:"))
}
```
entspricht das der folgenden anonymen Funktion:
```scala
val cont1 = (n: Int) => println(n + inputNumber("Second number:"))
```

Die Continuation zum Zeitpunkt der zweiten Eingabe entspricht dem folgenden Wert:
```scala
val cont2 = (m: Int) => println(n + m)
```

Durch das Ablegen dieser Continuations in einer Map und der Ausgabe des zugehörigen Schlüssels können wir `webRead` umgestalten und es dem Nutzer ermöglichen, die Auswertung ab dem letzten Zwischenstand fortzusetzen.
```scala
val continuations = new mutable.HashMap[String, Int=>Nothing]
var nextIndex : Int = 0
def getNextId : String = {
  nextIndex += 1
  "c"+nextIndex
}

def webRead_k(prompt: String, k: Int => Nothing) : Nothing = {
  val id = getNextId
  continuations += (id -> k)
  println(prompt)
  println("To continue, invoke continuation "+id)
  sys.error("Program terminated")
}

def continue(kId: String, result: Int): Nothing = continuations(kId)(result)

def webProg =
  webRead_k("First number:", (n: Int) =>
    webRead_k("Second number:", (m: Int) =>
      webDisplay("Sum of "+n+" and "+m+" is "+(n+m))))
```

Nun kann zuerst `webProg` aufgerufen werden, es wird die ID für die nächste Continuation ausgegeben. Mit dieser ID und der ersten Zahl kann dann `continue` aufgerufen werden, die dabei ausgegebene ID kann dann `continue` mit der zweiten Zahl übergeben werden, woraufhin das Ergebnis angezeigt wird. Die Bezeichner `n` und `m` sind zum Zeitpunkt, zu dem `webDisplay` aufgerufen wird, durch Scalas interne Closures gebunden und können korrekt aufgelöst werden.

In Bezug auf Webprogrammierung entsprechen die Continuations Zwischenzuständen der Auswertung, die serverseitig hinterlegt werden. Dabei erhält der Client (im Hintergrund) einen Bezeichner, um diesen Zustand mit der nächsten Eingabe aufzurufen. Somit könnte auch beim Klonen des Tabs oder bei Verwendung des "Zurück"-Buttons das Programm in allen Instanzen korrekt fortgesetzt werden. Das steht im Kontrast zu einer Implementierung mit einer _Session_, wobei der Client (und nicht der Zustand) anhand einer übermittelten Session-ID erkannt wird. In diesem Fall könnte es bei mehreren Instanzen zu fehlerhaften Ergebnissen kommen, da alle Instanzen über eine Session-ID laufen und damit nicht unabhängig sind.

Diese Eigenschaft ist auch bei unsere Implementierung erkennbar, es kann eine ausgegebene ID mehrfach aufgerufen werden, wobei die Auswertung jeweils dann mit der nächsten ID korrekt fortgesetzt werden kann und das korrekte Ergebnis liefert.

Eine Continuation kann als Repräsentation des Call-Stacks in einer Funktion aufgefasst werden.

## Rekursion im Web-Stil
Betrachten wir nun den allgemeineren Fall der n-fachen Addition: Im folgenden Programm wird eine Liste von Gegenständen rekursiv durchlaufen, wobei der Nutzer für jeden Gegenstand aufgefordert wird, einen Preis einzugeben. Nachdem alle Listenelemente abgearbeitet wurden, wird die Summe der Zahlen ausgegeben.

```scala
def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def addAllCosts(il: List[String]): Int = il match {
  case List() => 0
  case first :: rest => inputNumber("Cost of "+first+":") + addAllCosts(rest)
}

val testList = List("Banana", "Apple", "Orange")
def test() : Unit = println("Total cost: " + addAllCosts(testList))
```

Dieses Programm besitzt nach der "Web-Stil"-Transformation die Form:
```scala
def addAllCosts_k(il: List[String], k: Int => Nothing) : Nothing = il match {
  case List() => k(0)
  case first :: rest =>
    webRead_k("Cost of "+first+":",
      (n: Int) => addAllCosts_k(rest, (m: Int) => k(m+n)))
}

def testWeb() : Unit = addAllCosts_k(testList, m => webDisplay("Total cost: "+m))
```

Die Funktion `addAllCosts_k` wird aufgerufen mit der Liste von Gegenständen und der Contination `m => webDisplay("Total cost: "+m)`. Im Fall der leeren Liste wird die Continuation `k` auf `0` aufgerufen, es würde also `Total cost: 0` angezeigt werden. Ist die Liste nicht leer, so wird durch `webRead_k` der Nutzer dazu aufgefordert, den Preis des ersten Gegenstandes einzugegeben. Dabei wird die Continuation `n => addAllCosts_k(rest, m => k(m+n))` übergeben, diese Continuation wird also vom Nutzer/Client als nächstes mit dem entsprechenden Preis aufgerufen. Dabei wird die Continuation `m => k(m+n)` an `addAllCosts_k` zurückgereicht, d.h. die Nutzereingabe `n` wird jeweils den Kosten hinzugefügt.

Die Funktion `addAllCosts` lässt sich geschickt mit `map` umformulieren:
```scala
def addAllCostsMap(items: List[String]) : Int = {
  items.map((s: String) => inputNumber("Cost of " + s + ":")).sum
}
```

Würden wir auf dieser Implementation die Web-Transformation anwenden wollen, so müssten wir auch `map` transformieren. Die Web-Transformation ist also "allumfassend" und betrifft alle Funktionen, die in einem Programm auftreten (bis auf primitive Operationen). Es benötigen alle Funktionen einen Continuation-Parameter, damit sie auch innerhalb anderer Programme im Web-Stil eingesetzt werden sollen. Wir müssen in diesem Fall also `map` im Web-Stil verfassen. Oben ist die "normale" Implementation von `map`, unten die Web-transformierte Variante:
```scala
def map[X,Y](l: List[X], f: X => Y) : List[Y] = c match {
  case List() => List()
  case first::rest => f(first)::map(rest,f)
}

def map_k[X,Y](l: List[X],
               f: X, (Y => Nothing) => Nothing,
               k: List[Y] => Nothing) : Nothing = l match {
  case List() => k(List())
  case x::xs => f(x, y => map_k(xs, f, (ys: List[Y]) => k(y::ys)))
}
```

Bei der Transformation wird überall der Rückgabetyp mit `Nothing` ersetzt, die ursprünglichen Rückgabewerte werden stattdessen an die Continuation gereicht. So wird `f` nun durch ein neues Argument mit dem Typ `Y => Nothing` ergänzt, der Typ der Eingabe entspricht dem ursprünglichen Rückgabetyp. Die `map`-Funktion selbst wird durch ein neues Argument mit dem Typ `List[T] => Nothing` ergänzt, auch hier entspricht der Typ der Eingabe dem ursprünglichen Rückgabetyp.

Die Auswertungsreihenfolge ist entscheidend für die Web-Transformation, die implizite Auswertungsreihenfolge des Ausgangsprogramms (etwa bei geschachtelten Ausdrücken) muss explizit ausformuliert werden, das Programm wird _sequentalisiert_. 

Außerdem ist die Transformation global, es müssen alle verwendeten Funktionen (auch aus Bibliotheken etc.) transformiert werden. 

Aufgrund des Aspekts der Sequentialisierung ist die Transformation mit Continuations auch für das Programmieren von Compilern relevant. Man spricht bei diesem "Web-Stil" auch von _Continuation Passing Style_ (_CPS_).

# Continuation Passing Style
Programme in CPS besitzen die folgenden Eigenschaften:
- Alle Zwischenwerte besitzen einen Namen.
- Die Auswertungsschritte werden sequentialisiert (und die Auswertungsreihenfolge ist somit explizit).
- Alle Ausdrücke erhalten einen Continuation-Parameter und liefern keinen Rückgabewert (Rückgabetyp `Nothing`), sondern rufen den Continuation-Parameter auf ihrem Ergebnis auf.
- Alle Funktionsaufrufe sind _Tail Calls_ (da Funktionen nicht zurückkehren).

:::info
Man spricht von einem **Tail Call**, wenn bei einem rekursiven Aufruf im Rumpf einer Funktion keine weitere Berechnung nach der Rückkehr des Aufrufs stattfindet.
Der Aufruf `f(n+1)` in `def f(n: Int): Int = f(n+1)` ist ein Tail Call, in `def f(n: Int): Int = f(n+1)*2` jedoch nicht.

Liegt nach der CPS-Transformation eine "triviale" Continuation (d.h. `k` bleibt unverändert) bei einem rekursiven Aufruf vor, so lag ursprünglich ein Tail Call vor. Das wird bspw. an der folgenden Funktion deutlich:
```scala
def sumAcc(n: Int, acc: Int) : Int = l match {
  case 0 => acc
  case n => sumAcc(n-1, n+acc)
}

def sumAcc_k(n: Int, acc: Int, k: Int => Nothing) : Nothing = l match {
  case 0 => k(acc)
  case n => sumAcc_k(n-1, n+acc, k)
}
```
Die Funktion berechnet die Summe aller Zahlen von 1 bis `n`, verwendet aber im Gegensatz zu einer herkömmlichen Lösung (`case n => n+sum(n-1)`, siehe `sum` im Beispiel weiter unten) einen zusätzlichen Parameter, in dem die aktuelle Zwischensumme gehalten wird. Dadurch liegt im rekursiven Fall ein Tail Call vor und in der CPS-transformierten Variante wird `k` unverändert weitergereicht, was bei der herkömmlichen Lösung nicht der Fall wäre.

Bei Rekursion mit Tail Call (_Endrekursion_) muss der Kontext des rekursiven Aufrufs nicht auf dem Call Stack gespeichert werden, in Scala wird deshalb einfache Endrekursion erkannt und entsprechend optimiert. In Java werden auch endrekursive Aufrufe auf dem Stack hinterlegt, wodurch rekursive Berechnungen immer einen größeren Speicherverbrauch haben als iterative. In Racket findet bei Endrekursion nie eine "Kontextanhäufung" auf dem Call-Stack statt.
:::

Bei der CPS-Transformation von Funktionen und Werten sind die folgenden Schritte notwendig:
1. Ersetzen aller Rückgabetypen durch `Nothing`, wobei auch der Typ bei Konstanten `c: T` durch `T => Nothing` ersetzt wird

2. Ergänzen eines Parameters `k` mit Typ `R => Nothing`, wobei `R` der ursprüngliche Rückgabetyp ist (Konstanten der Form `c: T` werden also in Funktionen der Form `c_k(k: T => Nothing): Nothing` umgewandelt)

3. Weitergabe des Ergebnisses an `k`, bei Konstante `val c: T = x` etwa `def c_k(k: T => Nothing): Nothing = k(x)`

4. Sequentialisierung durch Weiterreichen der Zwischenergebnisse, aus `f(f(42))` wird bspw. `k => f_k(42, fRes => f_k(fRes,k))` oder aus `f(1) + g(2)` wird `k => f_k(1, fRes => g_k(2, gRes => k(fRes+gRes)))`

Weitere Beispiele der CPS-Transformation:
```scala
// constant value
val x: Int = 42
def x_k (k: Int => Nothing) : Nothing = k(42)

// recursion with "context"
def sum(n: Int) : Int = n match {
  case 0 => 0
  case n => n + sum(n-1)
}
def sum_k(n: Int, k: Int => Nothing) : Nothing = n match {
  case 0 => k(0) // k called with result
  case n => sum_k(n-1, m => k(n+m)) // non-trivial continuation
}

// two-way tail call recursion
def even(n: Int) : Boolean = n match {
  case 0 => true
  case n => odd(n-1)
}
def odd(n: Int) : Boolean = n match {
  case 0 => false
  case n => even(n-1)
}

def even_k(n: Int, k: Boolean => Nothing) : Nothing = n match {
  case 0 => k(true)
  case n => odd_k(n-1,k) // trivial continuation
}
def odd_k(n: Int, k: Boolean => Nothing) : Nothing = n match {
  case 0 => k(false)
  case n => even_k(n-1,k) // trivial continuation
}
```


# Automatische CPS-Transformation
Nun wollen wir die CPS-Transformation von Ausdrücken in FAE automatisieren. Die Transformationsregeln können folgendermaßen formalisiert werden:

- **Konstanten:** Aus `c` wird `k => k(c)`

- **Funktionsdefinitionen:** Aus `x => y` wird `k => k( (x, dynK) => dynK(y) )`

- **Funktionsapplikationen:** Aus `f(x)` mit `f: X => Y` wird `(k: Y => ...) => f_k(x, y => k(y))`

Im Fall von Funktionsdefinition müssen zwei Continuations beachtet werden: Die Continuation vom Zeitpunkt der Definition (`k`) sowie die _dynamische Continuation_ (`dynK`), die bei der Funktionsapplikation überreicht wird.

Wir definieren einen zweiten Typ neben `Exp` um CPS-transformierte Ausdrücke zu repräsentieren, da sich zum einen die Syntax mancher Sprachkonstrukte durch die Transformation ändert (es kommt der zusätzliche Continuation-Parameter hinzu) und zum anderen um die Eigenschaften von CPS-transformierten Programmen explizit zu formulieren (und sicherzustellen).
```scala
sealed abstract class CPSExp
sealed abstract class CPSVal extends CPSExp
case class CPSNum(n: Int) extends CPSVal
case class CPSFun(x: String, k: String, body: CPSExp) extends CPSVal
case class CPSCont(v: String, body: CPSExp) extends CPSVal

case class CPSVar(x: String) extends CPSVal { override def toString: String = x }
implicit def string2cpsExp(s: String): CPSVar = CPSVar(s)

case class CPSAdd(l: CPSVar, r: CPSVar) extends CPSVal
case class CPSFunApp(f: CPSVar, a: CPSVar, k: CPSVar) extends CPSExp
case class CPSContApp(k: CPSVal, a: CPSVal) extends CPSExp


```

Wir unterscheiden zwei syntaktische Kategorien, nämlich `CPSVal` (Werte) und `CPSExp` (Ausdrücke, die keinen Rückgabewert besitzen). Addition betrachten wir als primitive Operation, die nicht CPS-transformiert wird, und zählen diese somit zu `CPSVal`. Zu den Werten gehört auch die Repräsentation von Bezeichnern, `CPSVar`. Es wird zwischen Funktionsdefinitionen und Continuations unterschieden, wodurch auch die Applikation von Funktionen und Continuations getrennte Sprachkonstrukte sind. Bei Funktions- und Continuationapplikationen haben die Argumente den Typ `CPSVar`, somit kann es sich nicht um geschachtelte Ausdrücke handeln, diese würden nämlich nicht CPS entsprechen.

Wir benötigen wieder einen Mechanismus, um "frische" Bezeichner zu generieren, diesen übernehmen von unserem substitutionsbasierten FAE-Interpreter:
```scala
def freeVars(e: Exp) : Set[String] =  e match {
   case Id(x) => Set(x)
   case Add(l,r) => freeVars(l) ++ freeVars(r)
   case Fun(x,body) => freeVars(body) - x
   case App(f,a) => freeVars(f) ++ freeVars(a)
   case Num(n) => Set.empty
}
def freshName(names: Set[String], default: String) : String = {
  var last : Int = 0
  var freshName = default
  while (names contains freshName) { freshName = default+last; last += 1; }
  freshName
}
```

Die CPS-Transformation von FAE-Ausdrücken läuft folgendermaßen ab:
```scala
def cps(e: Exp) : CPSCont = e match {
  case Num(n) => {
    CPSCont("k", CPSContApp("k", CPSNum(n)))
  }
  case Id(x) => {
    val k = freshName(freeVars(e),"k")
    CPSCont(k, CPSContApp(k, CPSVar(x)))
  }
  case Add(l,r) => {
    val k = freshName(freeVars(e),"k")
    val lv = freshName(freeVars(r),"lv")
    CPSCont(k, CPSContApp(cps(l), CPSCont(lv, 
      CPSContApp(cps(r), CPSCont("rv", 
        CPSContApp(k, CPSAdd(lv,"rv")))))))
  }
  case Fun(p,b) => {
    val k = freshName(freeVars(e),"k")
    val dynK = freshName(freeVars(e),"dynK")
    CPSCont(k, CPSContApp(k, CPSFun(p, dynK, CPSContApp(cps(b), dynK))))
  }
  case App(f,a) => {
    val k = freshName(freeVars(e),"k")
    val fv = freshName(freeVars(a),"fv")
    CPSCont(k, CPSContApp(cps(f), CPSCont(fv,
      CPSContApp(cps(a), CPSCont("av",
        CPSFunApp(fv,"av",k))))))
  }
}
```

Entsprechend der zu Beginn formulierten Regeln zur Transformation werden Konstanten (`Num`- und `Id`-Ausdrücke) umgewandelt von `c` in `k => k(c)`, was als `CPSExp` dem Ausdruck `CPSCont(k, CPSContApp(k, c))` entspricht. Im `Add`- und `App`-Fall werden die zwei Unterausdrücke sequentiell umgewandelt, wobei die Zwischenergebnisse jeweils an einen Bezeichner (`CPSVar`, implizite Umwandlung von Strings) gebunden werden. 

Der Bezeichner `k` darf jeweils nicht in `e` vorkommen, der Bezeichner für den transformierten linken Unterausdruck (`lv` bzw. `fv`) darf nur nicht im rechten Unterausdruck vorkommen und der Bezeichner für den transformierten rechten Unterausdruck kann frei gewählt werden, da zwischen dem bindenden Vorkommen und der Verwendung kein rekursive Transformation eines Unterausdrucks stattfindet.

Im `Fun`-Fall wird der Rumpf mit der dynamischen Continuation transformiert und wird auch in den `CPSFun`-Ausdruck eingefügt. Auf diesen wird dann die Continuation vom Zeitpunkt der Definition angewendet.

Bei der hier verwendeten Transformation handelt es sich um die sogenannte _Fischer-Transformation_.

:::info
Die **Fischer-CPS-Transformation** ist ein möglicher CPS-Transformationsalgorithmus von vielen verschiedenen. Der Vorteil der Algorithmus ist seine Einfachheit und die Tatsache, dass es sich um eine strukturelle Rekursion des abstrakten Syntaxbaums handelt. Ein großer Nachteil ist aber, dass sogenannte _administrativen Redexe_ bei der Umwandlung entstehen, dabei handelt es sich um Contination-Applikationen von anonymen Funktionen, die nicht im ursprünglichen Programm enthalten waren und direkt aufgelöst werden könnten.

Bspw. ergibt die Fischer-Transformation von `Add(2,3)` den Ausdruck
```scala
CPSCont("k", CPSContApp(CPSCont("k", CPSContApp("k",2)),
  CPSCont("lv", CPSContApp(CPSCont("k", CPSContApp("k",3)),
    CPSCont("rv", CPSAdd("rv","lv"))))))
```
anstelle von 
```scala
CPSCont("k", CPSContApp("k", CPSAdd(2,3)))
```

Fortgeschrittenere Transformationsalgorithmen versuchen möglichst viele dieser administrativen Redexe zu vermeiden.
:::


# First-Class Continuations
In Programmiersprachen mit _First-Class Continuations_ (bspw. Scheme, Racket) gibt es Sprachkonstrukte, um die aktuelle Continuation zu jedem Zeitpunkt abzugreifen und um damit zu arbeiten (also um die Continuation zu reifizieren, zu binden, als Parameter zu übergeben oder aufzurufen). Mit solch einem Sprachfeature kann der Programmierer bspw. fortgeschrittene Kontrollstrukturen selbst definieren.

In Racket gibt es die Funktion `let/cc`, mit der die aktuelle Continuation an einen Identifier gebunden und im Rumpf von `let/cc` aufgerufen werden kann.
```scheme
[>] (number->string (+ 1 (let/cc k (string-length (k 3)))))
"4"
```

Im obigen Beispiel werden die Funktionsaufrufe vor dem Aufruf von `let/cc` als Continuation an `k` gebunden, im Rumpf von `let/cc` wird dann `k` mit `3` aufgerufen und damit die Continuation fortgesetzt, es werden die in der Contination gespeicherten Funktionsaufrufe angewendet und `"4"` ausgegeben. Der Aufruf von `k` kehrt nicht zurück, wodurch der Funktionsaufruf von `string-length` zwischen `let/cc` und `k` nicht mehr auf das Ergebnis angewendet wird. 

Die Continuation kann auch durch `set!` an einen globalen Identifier gebunden werden, um sie außerhalb des Rumpfes von `let/cc` aufrufen zu können:
```scheme
[>] (define c "dummy")
[>] (number->string (+ 1 (let/cc k (begin (set! c k) (k 3)))))
"4"
[>] (c 5)
"6"
```


# FAE mit First-Class-Continuations
Nun wollen wir unseren FAE-Interpreter First-Class-Continuations als Sprachfeature hinzufügen. Wir ergänzen dazu das folgende Sprachkonstrukt:
```scala
case class LetCC(param: String, body: Exp) extends Exp
```

Bei einem Aufruf von `LetCC` soll, wie in Racket, die aktuelle Contination an den Bezeichner `param` gebunden werden, wobei die Bindung im Rumpf von `LetCC` gültig ist.

In einem Programm, das bereits in CPS vorliegt, wäre das Bestimmen der aktuellen Continuation trivial. Für unsere Implementation von `LetCC` wollen wir aber nicht alle Programme transformieren, sondern stattdessen den Interpreter selbst in CPS verfassen. Continuations auf Interpreter-Ebene repräsentieren zugleich die noch durchzuführende Auswertung auf Ebene der Objektsprache. 

Bei einer Implementierung durch die automatische CPS-Transformation von Programmen müsste jedes Programm erst transformiert werden. Der Interpreters muss hingegen nur ein Mal transformiert werden. Der erste Schritt zur Implementierung von `LetCC` ist also das Transformieren des Interpreters.

## CPS-Transformation des Interpreters
Wir beginnen mit unserem FAE-Interpreter:
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
    case ClosureV(f,cEnv) =>
      eval(f.body, cEnv+(f.param -> eval(a,env))) // call-by-value
    case _ => sys.error("Can only apply functions")
  }
}
```

Wir ersetzen den Rückgabetypen mit `Nothing` und ergänzen einen Continuation-Parameter `k` mit dem Typ `Value => Nothing`. Der `Num`-, `Id`- und `Fun`-Fall sind trivial, da diese nicht rekursiv sind, hier reichen wir das Ergebnis einfach an `k` weiter. Im `Add`- und `App`-Fall muss die Auswertung des linken und rechten Unterausdrucks sequentialisiert werden, wir werten von links nach rechts aus.
```scala
def eval(e: Exp, env: Env, k: Value => Nothing) : Nothing = e match {
  case Num(n) => k(NumV(n))
  case Id(x) => k(env(x))
  case Add(l,r) => eval(l, env, lv => eval(r, env, rv => (lv,rv) match {
    case (NumV(a),NumV(b)) => k(NumV(a+b))
    case _ => sys.error("Can only add numbers")
  }))
  case f@Fun(_,_) => k(ClosureV(f,env))
  case App(f,a) => eval(f, env, fv => fv match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(a, env, av => eval(b, cEnv+(p -> av), k))
    case _ => sys.error("Can only apply functions")
  })
}
```

Im `Add`-Fall werten wir erst den linken Unterausdruck aus und übergeben dabei eine Continuation, die das Ergebnis an `lv` bindet und mit der Auswertung des rechten Unterausdrucks fortfährt. Deren Ergebnis wird durch die übergebene Continuation an `rv` gebunden. Wie im ursprünglichen Interpreter werden nun durch Pattern Matching die Zahlen extrahiert und ein `NumV`-Objekt mit deren Summe erzeugt, dieses wird nun aber an `k` überreicht.

Im `App`-Fall wird auch erst der linke Teilausdruck ausgewertet und diesmal durch die Continuation an `fv` gebunden, durch Pattern Matching werden Parameter und Rumpf der Funktion sowie die Umgebung im Closure gebunden. Nun wird das Argument ausgewertet, durch die übergebene Continuation an `av` gebunden und zuletzt `eval` mit dem Rumpf, der Umgebung inkl. neuer Bindung und der Continuation `k` aufgerufen.

Um den Interpreter auch ohne Rückgabe testen zu können, verwenden wir folgende Funktion:
```scala
def startEval(e: Exp) : Value = {
  var res: Value = null
  val s: Value => Nothing = v => { res = v; sys.error("Program terminated") }
  try { eval(e, Map(), s) } catch { case _: Throwable => () }
  res
}
```

Es wird `eval` eine Continuation überreicht, die das Ergebnis der Auswertung bindet und dann mit einem Fehler die Auswertung beendet (um den Rückgabetyp `Nothing` zu erfüllen). Die Auswertung selbst wird in einem Try-Catch-Block gestartet, um den Fehler abzufangen. Das durch die Continuation gebundene Ergebnis wird von `startEval` ausgegeben.

Die CPS-Transformation des Interpreters hat zur Folge, dass der Interpreter nicht mehr vom Call-Stack der Hostsprache abhängig ist, da Programme in CPS diesen nicht verwenden.

## Implementierung von 'LetCC'
Jetzt wo der Interpreter selbst CPS-transformiert ist, können wir das neue Sprachkonstrukt `LetCC` mit wenig Aufwand ergänzen, da wir die Continuations auf Interpreter-Ebene als Continuations der Objektsprache nutzen können. Zuerst erweitern wir `Exp` um das Sprachkonstrukt `LetCC`:
```scala
case class LetCC(param: String, body: Exp) extends Exp
```

Im Interpreter ergänzen wir den `LetCC`-Fall, hier setzen wir die Auswertung rekursiv im Body fort, wobei wir den Parameter in `LetCC` an die aktuelle Continuation `k` binden. Da `k` den Typ `Value => Nothing`, die Umgebung aber den Typ `Map[String,Value]` besitzt, können wir die Continuation nicht direkt in der Umgebung binden. Stattdessen erweitern wir `Value` um den Fall `ContV`:
```scala
case class ContV(k: Value => Nothing) extends Value
```

Nun sind Continations eine Werte-Art und können wie andere Werte an Identifier gebunden werden, wordurch wir den `LetCC`-Fall verfassen können:
```scala
case LetCC(p,b) => eval(b, env+(p -> ContV(k)))
```

Es fehlt noch die Applikation von Continuations, hierzu überladen wir das `App`-Konstrukt, so dass damit sowohl Funktionen als auch Continuations angewendet werden können. Im `App`-Fall müssen wir nun die Fallunterscheidung um einen `ContV`-Fall neben dem `ClosureV`-Fall erweitern:
```scala
case App(f,a) => eval(f, env, fv => fv match {
  case ClosureV(Fun(p,b),cEnv) =>
    eval(a, env, av => eval(b, cEnv+(p -> av), k))
  case ContV(k2) => eval(a, env, av => k2(av))
  case _ => sys.error("Can only apply functions")
})
```

Im `ContV`-Zweig werten wir das Argument aus und übergeben dabei eine Continuation, die das Ergebnis an `av` bindet und dann der aufzurufenden Continuation `k2` aus dem `ContV`-Objekt `av` überreicht. Dabei wird die aktuelle Continuation `k` ignoriert, die Auswertung "springt" ohne zurückzukehren. Der Wechsel zur Continuation `k2` kann als ein Austauschen des Call-Stacks aufgefasst werden.

Durch das "Wrappen" der Interpreter-Continuations (auf Ebene der Metasprache) können wir diese also als Continuations für die Objektsprache verwenden.


# Delimited Continuations
Die (_Undelimited_) Continuations, die wir bisher kennen gelernt haben, führen bei einem Aufruf zu einem "Sprung" (ähnlich zu einer `GOTO`-Anweisung), wobei die Auswertung nicht mehr zur Stelle des Aufrufs zurückkehrt. Somit ist keine Komposition (d.h. "Nacheinanderschalten" und damit Kombinieren) von Continuations möglich.

Dies ist mit _Delimited Continuation_ möglich, sie repräsentieren nur einen Ausschnitt des Call-Stacks (während _Undelimited Continuations_ den gesamten Call-Stack repräsentieren) und besitzen wie gewöhnliche Funktionen einen Rückgabewert. Dadurch ist die Komposition von Delimited Continuations möglich, sie werden deshalb auch als _Composable Continuations_ bezeichnet.

Delimited Continuations sind ein sehr mächtiges Sprachkonstrukt und erlauben bspw. die Programmierung von fortgeschrittenem Exception Handling oder Backtracking-Algorithmen.

In der Racket-Bibliothek `racket/control` gibt es die zwei Funktionen `shift` und `reset`, mit denen Delimited Continuations erzeugt werden können. Dabei verhält sich `shift` ähnlich wie `let/cc`, wobei aber nur die Continuation ab dem (im AST) nächstliegenden Aufruf von `reset` gebunden wird, die Continuation wird also durch `reset` begrenzt (_delimitiert_) und repräsentiert nur den Ausschnitt des Call-Stacks zwischen `reset` und `shift`.
```scheme
(* 2 (reset (+ 1 (shift k (k 5)))))
```

Im obigen Beispiel wird die Continuation, die durch `shift` an `k` gebunden wird, durch `reset` auf den Aufruf von `+` mit `1` beschränkt. Wird `k` auf `5` aufgerufen, so wird nur die Addition auf `5` durchgeführt, die Multiplikation vor `reset` gehört nicht zur Continuation. Somit entspricht die Berechnung `(* 2 (+ 1 5))` und das Ergebnis ist `12`. Da es sich bei `k` um eine Delimited Continuation handelt, ist auch Continuation-Komposition möglich:
```scheme
(* 2 (reset (+ 1 (shift k (k (k 5))))))
```

In diesem Fall wird `k` zwei Mal auf `5` angewendet, die Berechnung entspricht also 
`(* 2 (+ 1 (+ 1 5)))` und das Ergebnis ist `14`.

## Interpreter mit Delimited Continuations
Wir können im CPS-transformierten Interpreter mit wenig Aufwand Sprachkonstrukte hinzufügen, die `shift` und `reset` aus Racket entsprechen. Wir ergänzen dazu `Exp` um die zwei neuen Cases `Shift` und `Reset`:
```scala
case class Shift(param: String, body: Exp) extends Exp
case class Reset(body: Exp) extends Exp
```

Wir müssen den Typ des Interpreters ändern, da Aufrufe von Delimited Continuations zurückkehren und die Berechnung nicht abbrechen. Dadurch haben Continuations den Typ `Value => Value` und der Interpreter gibt einen `Value` zurück. Im Interpreter ergänzen wir die entsprechenden Fälle und passen den `App`-Fall an (Änderungen mit `<--` markiert):
```scala
def eval(e: Exp, env: Env, k: Value => Value) : Value = e match {
  // ...
  case App(f,a) => eval(f, env, fv => fv match {
    case ClosureV(Fun(p,b),cEnv) =>
      eval(a, env, av => eval(b, cEnv+(p -> av), k))
    case ContV(k2) => eval(a, env, av => k(k2(av))) // <--
    case _ => sys.error("Can only apply functions")
  })
  case Reset(e) => k(eval(e,env,x=>x)) // <--
  case Shift(p,b) => eval(b, env+(p -> ContV(k)), x=>x) // <--
}
```

Im `Reset`-Fall wird die aktuelle Continuation zurückgesetzt, indem die Identitätsfunktion anstelle von `k` weitergereicht wird. Im `Shift`-Fall wird wie im `LetCC`-Fall die aktuelle Continuation "umhüllt" und in der Umgebung an den Identifier gebunden. Außerdem wird hier auch die Continuation zurückgesetzt (weil dies dem Verhalten in Racket entspricht). 

Im `App`-Fall wird bei der Applikation von Continuations die aktuelle Continuation nun nicht mehr verworfen, sondern wird mit dem Ergebnis der Continuation-Applikation aufgerufen, d.h. es findet eine Komposition der aktuellen Continuation nach der aufgerufenen Continuation statt.


# Monaden
In unseren bisherigen Interpretern haben wir bereits einige verschiedene "Rekursions-Patterns" gesehen: 
- Im [ersten Interpreter](https://pad.its-amazing.de/programmiersprachen1teil1#Erster-Interpreter-AE) haben wir direkte Rekursion, bei der die `eval`-Funktion rekursiv auf den Unterausdrücken aufgerufen wird. Die rekursive Auswertung entspricht exakt der rekursiven Datenstruktur, in denen die Programme repräsentiert sind (_strukturelle Rekursion_).

- Bei der Einführung von Environments in [AEId](https://pad.its-amazing.de/programmiersprachen1teil1#Identifier-mit-Umgebung-AEId) oder in [FAE](https://pad.its-amazing.de/programmiersprachen1teil1#Closures) wird die Environment bei der rekursiven Auswertung im abstrakten Syntaxbaum (AST) nach unten weitergereicht, d.h. der zusätzliche Parameter `env` wird entlang der Datenstruktur propagiert. 

- Bei der Ergänzung von mutierbaren Boxen in [BCFAE](https://pad.its-amazing.de/programmiersprachen1teil1#Interpreter) haben wir den `Store`-Parameter hinzugefügt, der immer von der aktuellen Auswertungsposition zur nächsten Auswertungsposition gereicht (und dazwischen potentiell modifizert) wird.

- Der [CPS-transformierte Interpreter](#FAE-mit-First-Class-Continuations) entspricht dem Continuation Passing Style, den wir bereits ausführlich besprochen haben. 

_Monaden_ sind eine Möglichkeit, über solche (und noch viel mehr) Funktionskompositions-Patterns zu abstrahieren. Durch Monaden ist es möglich, einmalig verfassten Code in all diese Stile zu übersetzen.

Zur Einführung von Monaden wollen wir aber vorerst einen anderen Stil betrachten.

## Einführung mit Option-Monade
```scala
def expr = h(!g(f(27)+"z"))
```

Angenommen, die Funktionen `f` und `h`, die im obigen Ausdruck aufgerufen werden, können in manchen Fällen keine Ausgabe liefern (was bspw. daran liegen könnte, dass sie intern Anfragen über ein Netzwerk schicken). In solch einem Fall wäre es sinnvoll, den `Option`-Datentyp zu verwenden, um auch bei fehlgeschlagener Auswertung `None` ausgeben zu können. Bei erfolgreicher Auswertung wird das Ergebnis mit `Some()` umhüllt:
```scala
def f(n: Int) : Option[String] = if (n < 100) Some("x") else None
def g(x: String) : Option[Boolean] = Some(x == "x")
def h(b: Boolean) : Option[Int] = if (b) Some(27) else None
```

Da aber Aufrufe der Funktionen potentiell `None` anstelle eines Ergebnisses liefern, muss `expr` modifiziert werden:
```scala
def expr = f(27) match {
  case Some(x) => g(x+"z") match {
    case Some(y) => h(!y)
		case None => None
  }
	case None => None
}
```

Bei jedem Aufruf muss anschließend ein Pattern-Match genutzt werden, um die zwei Fälle (`None` und `Some()`) zu unterscheiden und im `Some()`-Fall die Auswertung mit dem Ergebnis fortzusetzen. Es ist erkennbar, dass dieses Pattern bei jedem Aufruf einer Funktion mit Rückgabetyp `Option[T]` auftritt, wir abstrahieren also über das Pattern mit der folgenden Funktion:
```scala
def bindOption[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
  case Some(x) => f(x)
  case None => None
}

def expr = 
  bindOption(f(27), (x: String) => 
    bindOption(g(x+"z"), (y: Boolean) =>
	  h(!y)))
```

Mit `bindOption` können wir jeden Ausdruck im "Option-Stil" vereinfachen und die Redundanz des wiederholten, gleichartigen Pattern-Matchings vermeiden.

Angenommen, auf das verneinte Ergebnis von `g` wird nicht mehr `h` angewendet:
```scala
def expr2 = !g(f(27)+"z")
```

Dann muss das Ergebnis vor der Ausgabe wieder mit `Some()` "verpackt" werden, damit der Rückgabetyp `Option` (und damit der Option-Stil) erfüllt bleibt. Dies ist aber nicht mit `bindOption` möglich, sondern wir müssten `Some()` explizit aufrufen:
```scala
def expr2 =
  bindOption(f(27), (x: String) =>
    bindOption(g(x+"z"), (y: Boolean) =>
      Some(!y)))
```

Da wir aber über das Pattern der Funktionskomposition abstrahieren wollen, soll der `Option`-Datentyp nicht sichtbar sein. Wir fügen also unserem Funktionskompositions-Interface stattdessen neben `bindOption` eine zweite Funktion `unitOption` hinzu:
```scala
def bindOption[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
  case Some(x) => f(x)
  case None => None
}

def unitOption[A](a: A) : Option[A] = Some(a)
```

Mit `unitOption` können wir `expr2` folgendermaßen ausdrücken:
```scala
def expr2 =
  bindOption(f(27), (x: String) =>
    bindOption(g(x+"z"), (y: Boolean) =>
      unit(!y)))
```

Nun haben wir den Option-Stil abstrahiert, wir können aber einen Schritt weiter gehen und über den Typ `Option` abstrahieren, um beliebige Patterns auszudrücken. Dadurch erhalten wir das _Monad-Interface_.

## Definition
```scala
trait Monad[M[_]] {
  def unit[A](a: A) : M[A]
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
}
```

Eine Monade ist ein Tripel aus einem Typkonstruktor (`M[_]`) und zwei Funktionen, nämlich `unit` und `bind`. Es müssen zudem die folgenden _Monadengesetze_ gelten:
- `bind(unit(x),f) == f(x)`
- `bind(x, y => unit(y)) == x`
- `bind(bind(x,f),g) == bind(x, y => bind(f(y),g))`

D.h. `unit` ist eine Art "neutrales Element" und `bind` ist eine assoziative Operation.

Monaden dienen zur Funktionskomposition für Fälle, in denen der Rückgabetyp einer Funktion nicht dem Parametertyp der danach anzuwendenden Funktion entspricht, sondern ein "Zwischenschritt" notwendig ist.

Mit dem Monad-Interface kann das Einführungsbeispiel folgendermaßen ausgedrückt werden:
```scala
object OptionMonad extends Monad[Option] {
  override def unit[A](a: A) : Option[A] = Some(a)
  override def bind[A,B](m: Option[A], f: A => Option[B]) : Option[B] = m match {
    case Some(y) => f(y)
    case None => None
  }
}

def expr2(m: Monad[Option]) =
  m.bind(f(27), (x: String) =>
    m.bind(g(x+"z"), (y: Boolean) =>
      m.unit(!y)))
      
val expr2Res = expr2(OptionMonad)
```

## For-Comprehension-Syntax
Die geschachtelten Aufrufe von `bind` sind bei komplexeren Beispielen etwas unleserlich, es kann aber die sogenannte _Monad-Comprehension_-Notation verwendet werden, um die Ausdrücke einfacher auszudrücken. Monad Comprehensions werden in Haskell und manchen anderen Sprachen nativ unterstützt, in Scala müssen wir stattdessen die _For-Comprehension_-Syntax für diese Zwecke "hijacken".

Diese Syntax wird im Normalfall für Listen und andere `Collection`-Datentypen verwendet:
```scala
val l = List(List(1,2),List(3,4))

val res = for {
  x <- l; 
  y <- x } yield y+1 // == List(2,3,4,5)
```

Durch Desugaring werden For-Comprehensions in Aufrufe von `flatMap` und `map` umgewandelt:
```scala
val res = l.flatMap(x => x.map(y => y+1))
```

Diese Syntax kann für alle Datentypen angewendet werden, für die `map` und `flatMap` definiert ist. Mit der folgenden Funktionsdefinition kann somit die Syntax der For-Comprehensions für das Programmieren mit Monaden genutzt werden:
```scala
implicit def monadicSyntax[A, M[_]](m: M[A])(implicit mm: Monad[M]) = new {
  def map[B](f: A => B): Any = mm.bind(m, (x: A) => mm.unit(f(x)))
  def flatMap[B](f: A => M[B]): M[B] = mm.bind(m, f)
}
```

Durch die obige implizite Definition, in der wir `map` und `flatMap` so definieren, dass sie gerade der `unit`- und der `bind`-Operation entsprechen, sorgen wir dafür, dass die For-Comprehension-Syntax für die `Monad`-Klasse genutzt werden kann.

Unser Option-Monad-Beispiel kann mit dieser Syntax wie folgt ausgedrückt werden:
```scala
def expr2(m: Monad[Option]) = for {
  x <- f(27);
  y <- g(x+"z") 
} yield !y
```


## Operationen auf Monaden
Es lassen sich einige nützliche Operationen generisch für beliebige Monaden definieren:

`fmap` wandelt jede Funktion mit Typ `A => B` in eine Funktion vom Typ `M[A] => M[B]` um.
```scala
def fmap[M[_],A,B](f: A => B)(implicit m: Monad[M]): M[A] => M[B] = 
  a => m.bind(a, (x: A) => m.unit(f(x)))

assert( fmap((n: Int) => n.toString)(OptionMonad)(Some(1)) == Some("1") )

```

`sequence` verknüpft eine Liste monadischer Werte zu einem einzelnen monadischen Wert, der eine Liste ist, aus einem Wert des Typs `List[M[A]]` wird ein Wert des Typs `M[List[A]]`.
```scala
def sequence[M[_],A](l: List[M[A]])(implicit m: Monad[M]) : M[List[A]] = l match {
  case x :: xs => 
      m.bind(x, (y: A) => 
        m.bind(sequence(xs), (ys : List[A]) =>
		  m.unit(y :: ys)))
  case Nil => m.unit(List.empty)
}

def ex(implicit m: Monad[Option]) =
  List(m.unit(1),m.unit(2),m.unit(3))

// List[Option[Int]] => Option[List[Int]]
assert( sequence(ex(OptionMonad))(OptionMonad) == Some(List(1,2,3)) )
```

`mapM` verknüpft `sequence` und `map`. Es wird also aus `List[A]` erst `List[M[B]]` (durch `map`) und anschließend aus `List[M[B]]` (durch `sequence`) `M[List[B]]`.
```scala
def mapM[M[_],A,B](f : A => M[B], l: List[A])(implicit m: Monad[M]) : M[List[B]] =
  sequence(l.map(f))

assert( mapM[Option,Int,String](n => Some(n.toString), List(1,2,3))(OptionMonad) 
    == Some(List("1","2","3")) )
```

`join` kann verwendet werden, um einen zweifach in einer Monade "verpackten" Wert zu "entpacken". Der Eingabetyp ist also `M[M[A]]`, der Ausgabetyp `M[A]`.
```scala
def join[M[_],A](x : M[M[A]])(implicit m: Monad[M]) : M[A] = 
  m.bind(x, (y : M[A]) => y)

assert( join[Option,Int](Some(Some(1)))(OptionMonad) == Some(1) )
```

## Weitere Monaden
++**Option-Monade:**++ Die _Option-Monade_ (auch _Maybe-Monade_ genannt) haben wir bereits in der [Einführung](#Einführung-mit-Option-Monade) kennengelernt.
```scala
object OptionMonad extends Monad[Option] {
  override def bind[A,B](a: Option[A], f: A => Option[B]) : Option[B] = a match {
    case Some(x) => f(x)
    case None => None
  }
  override def unit[A](a: A) = Some(a)
}	
```

++**Identitäts-Monade:**++ Die _Identitäts-Monade_ ist die einfachste Monade, die normaler Funktionsapplikation entspricht. Die Übergabe der Identitäts-Monade an monadischen Code liefert den Code im gewöhnlichen Programmierstil.

```scala
type Id[X] = X   
object IdentityMonad extends Monad[Id] {
  def bind[A,B](x: A, f: A => B) : B = f(x) 
  def unit[A](a: A) : A = a
}
```
Für diese Monade wird leider nicht die Syntax der For-Comprehensions unterstützt.

++**Reader-Monade:**++ Die _Reader-Monade_ kodiert den "Environment Passing Style", den wir bspw. im FAE-Interpreter gesehen haben.
```scala
trait ReaderMonad[R] extends Monad[({type M[A] = R => A})#M] {
  override def bind[A,B](x: R => A, f: A => R => B) : R => B = r => f(x(r))(r)
  override def unit[A](a: A) : R => A = _ => a
}
```

Beim Typparameter `({type M[A] = R => A})#M` handelt es sich um eine Funktion auf Typ-Ebene, d.h. `M[A]` wird über die Gleichung `M[A] = R => A` definiert, wobei `R` ein zusätzlicher Typparameter der Monade ist. `R` wird bei jedem Funktionsaufruf als zusätzliches Argument weitergereicht, was in `M[A] = R => A` durch Currying ausgedrückt wird.

++**State-Monade:**++ Bei der _State-Monade_ sind Berechnungen abhängig von einem Zustand `S`, der von Berechnung zu Berechnung gereicht wird. `S` wird also beim Aufruf übergeben und mit dem Ergebnis in einem Tupel ausgegeben, was mit Currying in der Gleichung `M[A] = S => (A,S)` ausgedrückt wird.
```scala
trait StateMonad[S] extends Monad[({type M[A] = S => (A,S)})#M] {
  override def bind[A,B](x: S => (A,S), f: A => S => (B,S)) : S => (B,S) = 
      s => x(s) match { case (a,s2) => f(a)(s2) }
  override def unit[A](a: A) : S => (A,S) = s => (a,s)
}
```

++**Listen-Monade:**++ Bei der _Listen-Monade_ erzeugen Berechnungen Listen von Ergebnissen, `bind` fügt jeweils alle Ergebnisse in einer Liste zusammen. In `bind` wird die Funktion `f` auf jedes Element der Liste angewendet und die Ergebnisse vom Typ `List[B]` werden zu einer Liste konkateniert.
```scala
object ListMonad extends Monad[List] {
  override def bind[A,B](x: List[A], f: A => List[B]) : List[B] = x.flatMap(f)
  override def unit[A](a: A) = List(a)
}  
```

++**Continuation-Monade:**++ Die _Continuation-Monade_ kodiert CPS, es gilt `M[A] = (A => R) => R`, wobei `R` ein zusätzlicher Typparameter ist, der den Rückgabetyp von Continuations angibt.
```scala
trait ContinuationMonad[R] extends Monad[({type M[A] = (A => R) => R})#M] {
  type Cont[X] = (X => R) => R
  override def bind[A,B](x: Cont[A], f: A => Cont[B]) : Cont[B] = 
     k => x( a => f(a)(k))
  override def unit[A](a: A) : Cont[A] = k => k(a)
  def callcc[A,B](f: (A => Cont[B]) => Cont[A]) : Cont[A] = 
    k => f( (a:A) => (_:B=>R) => k(a))(k)
}
```

## Monadentransformer
In der praktischen Programmierung will man oft die Eigenschaften verschiedener Monaden kombinieren, etwa um gleichzeitig die Option-Monade und die Listen-Monade zu nutzen. Mit _Monadentransformern_ ist die Komposition von Monaden möglich. Dabei handelt es sich um eine zusätzliche Fassung von jeder Monade, die mit einer weiteren Monade parametrisiert ist. 

Wir verwenden wieder die Option-Monade als Beispiel und erweitern diese um eine äußere Monade:
```scala
type OptionT[M[_]] = { type x[A] = M[Option[A]] }

class OptionTMonad[M[_]](val m: Monad[M]) extends Monad[OptionT[M]#x] {
  override def bind[A,B](x: M[Option[A]], f: A => M[Option[B]]) : M[Option[B]] =
    m.bind(x, (z: Option[A]) => z match {
      case Some(y) => f(y)
      case None => m.unit(None)
    })
  override def unit[A](a: A) : M[Option[A]] = m.unit(Some(a))
  
  def lift[A](x: M[A]) : M[Option[A]] = m.bind(x, (a: A) => m.unit(Some(a)))
}
```

`lift` nimmt einen Wert vom Typ `M[A]` und macht daraus einen Wert vom Typ `M[Option[A]]`, umhüllt also den inneren Datentyp von `M` mit `Option`.

:::warning
IO-Monad?
:::

# Monadischer Interpreter
## Monadenbibliothek
Bevor wir verschiedene Bausteine für Interpreter anlegen, wollen wir erst eine Bibliothek von Monaden und Monadenkompositionen anlegen. Dabei orientieren wir uns an dem Stil von Standardbibliotheken für Scala oder Haskell.
```scala
trait Monad {
  type M[_]
  def unit[A](a: A) : M[A]
  def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  implicit def monadicSyntax[A](m:M[A]) = new {
      def map[B](f: A => B) = bind(m, (x:A) => unit(f(x)))
      def flatMap[B](f: A => M[B]) : M[B] = bind(m,f)
  }
}
```

Wir definieren den Typkonstruktor `M` innerhalb der Klasse und nicht als Typparameter, da die "Typgleichungen" somit leichter ausgedrückt werden können.

Zuerst legen wir Interfaces für die Monaden an, in denen wir jeweils die Typgleichung für `M` und ggf. weitere benötigte Typen definieren. In den Interfaces legen wir auch zusätzlich benötigte Funktion neben `bind` und `unit` in abstrakter Form an. Wir definieren dann alle Monaden bis auf die Identitäts-Monade in Form von Monadentransformern, diesmal mit einer zusätzlichen inneren Monade. Dabei implementieren wir alle abstrakten Funktionen nun (unter Berücksichtigung der inneren Monade) konkret. Durch Komposition mit der Identitäts-Monade erzeugen wir effektiv eine Fassung ohne innere Monade, wodurch wir diese nicht getrennt definieren müssen. 

:::warning
Interfacing, Transformer, Bausteinsystem
:::

# Defunktionalisierung
:::info
**Defunktionalisierung** bezeichnet die Umwandlung von Higher-Order-Funktionen in First-Order-Funktionen. Diese Umwandlung ist sowohl eine Compilertechnik als auch eine Programmiertechnik.
:::

:::info
Eine **abstrakte Maschine** bezeichnet in der theoretischen Informatik einen endlichen Automaten dessen Zustandsmenge unendlich groß sein kann.
:::

In einem zu defunktionalisierenden Programm dürfen keine anonyme Funktionen auftreten. Um das Programm ohne anonyme Funktionen umzuschreiben, wird _Lambda Lifting_ (auch _Closure Conversion_ genannt) verwendet.

## Lambda Lifting
Ziel von _Lambda Lifting_ ist es, lokale Funktionen in Top-Level-Funktionen umzuwandeln. Lambda-Lifting kommt auch häufig in Compilern zum Einsatz, bspw. findet man im Bytecode, der beim Kompilieren von Scala-Programmen erzeugt wird, globale Funktionsdefinition für alle anonymen Funktionen im Programm.

Im folgenden Programm gibt es zwei anonyme Funktionen, nämlich `y => y*n` und `y => y+n`.
```scala
def map(f: Int => Int, l: List[Int]) : List[Int] = l match {
  case List() => List()
  case x::xs => f(x)::map(f,xs)
}

def addAndMulNToList(n: Int, l: List[Int]) : List[Int] = 
  map(y => y*n, map(y => y+n, l))
```

Diese müssen extrahiert und als Top-Level-Funktionen definiert werden:
```scala
val f = (n: Int) => (y: Int) => y+n
val g = (n: Int) => (y: Int) => y*n
```

Dadurch lässt sich die Funktion `addAndMultNToList` folgendermaßen ohne anonyme Funktionen umschreiben:
```scala
def addAndMulNToListLL(n: Int, l: List[Int]) : List[Int] = 
  map(g(n), map(f(n), l))
```

**Vorgehensweise:**
1. Anonyme Funktionen im Programm suchen und benennen
2. Den anonymen Funktionen entsprechende Top-Level-Funktionen anlegen
3. Code aus der ursprünglichen anonymen Funktion in die neue Funktion kopieren, freie Variablen suchen und als Parameter hinzufügen.
4. Eingabe der ursprünglichen anonymen Funktion durch Currying als weiteren Parameter ergänzen.
5. Anonyme Funktion im Programm durch Aufruf der neuen Top-Level-Funktion ersetzen
6. Lambda-Lifting im Rumpf der neuen Top-Level-Funktion fortsetzen, falls dort anonyme Funktionen auftreten

Nun wenden wir dieses Verfahren auf unseren CPS-transformierten Interpreter an. Hier ist die ursprüngliche Fassung des Interpreters, wobei alle anonymen Funktionen durch Kommentare benannt sind:
```scala
object CPSTransformed {
  def eval[T](e: Exp, env: Env, k: Value => T) : T = e match {
    case Num(n: Int) => k(NumV(n))
    case Id(x: String) => k(env(x))
    case Add(l: Exp, r: Exp) =>
      eval(l, env, lVal => /* addC1 */ 
        eval(r, env, rVal => /* addC2 */ (lVal,rVal) match {
        case (NumV(a),NumV(b)) => k(NumV(a+b))
        case _ => sys.error("Can only add numbers")
      }))
    case f@Fun(_,_) => k(ClosureV(f,env))
    case App(f,a) =>
      eval(f, env, fVal => /* appC1 */ fVal match {
        case ClosureV(Fun(p,b),cEnv) => eval(a, cEnv, aVal => /* appC2 */ 
          eval(b, env+(p -> aVal), k))
        case _ => sys.error("Can only apply functions")
      })
  }
}
```

Wir extrahieren die vier anonymen Funktionen und ersetzen ihre Verwendung durch Aufrufe der neuen Top-Level-Funktionen (entsprechend der obigen Schritte). Dadurch erhalten wir eine semantisch gleichbedeutende Fassung des Interpreters, in der aber keine anonymen Funktionen auftreten:
```scala
object LambdaLifted {
  def addC1[T](r: Exp, env: Env, k: Value => T)(lVal: Value): T =
    eval(r, env, addC2(lVal,k))
  def addC2[T](lVal: Value, k: Value => T)(rVal: Value): T = (lVal,rVal) match {
      case (NumV(a),NumV(b)) => k(NumV(a+b))
      case _ => sys.error("Can only add numbers")
    }
  def appC1[T](a: Exp, env: Env, k: Value => T)(fVal: Value) : T  = fVal match {
      case ClosureV(Fun(p,b),cEnv) => eval(a, cEnv, appC2(b,p,env,k))
      case _ => sys.error("Can only apply functions")
    }
  def appC2[T](b: Exp, p: String, env: Env, k: Value => T)(aVal: Value) : T =
    eval(b, env+(p -> aVal), k)

  def eval[T](e: Exp, env: Env, k: Value => T) : T = e match {
    case Num(n: Int) => k(NumV(n))
    case Id(x: String) => k(env(x))
    case Add(l: Exp, r: Exp) =>
      eval(l, env, addC1(r,env,k))
    case f@Fun(_,_) => k(ClosureV(f,env))
    case App(f,a) =>
      eval(f, env, appC1(a,env,k))
  }
}
```

## Defunktionalisierungsschritt
In unserem ersten Beispiel liegen nach dem Lambda-Lifting noch Higher-Order-Funktionen vor, nämlich `f` und `g`.
```scala
val f = (n: Int) => (y: Int) => y + n
val g = (n: Int) => (y: Int) => y * n

def addAndMulNToListLL(n: Int, l: List[Int]) : List[Int] =
  map(g(n), map(f(n), l))
```

Das Programm soll nun so umgeformt werden, dass nur First-Order-Funktionen auftreten. Um den Closure nach dem ersten Currying-Schritt zu repräsentieren, legen wir einen Datencontainer an, der für beide Funktionen den Wert von `n` halten kann.
```scala
sealed abstract class FunctionValue
case class F(n: Int) extends FunctionValue
case class G(n: Int) extends FunctionValue
```

Außerdem legen wir eine Funktion `apply` an, die mit Instanzen von `FunctionValue` zusammen mit dem zweiten Argument aufgerufen werden kann, um den zweiten Schritt des Currying durchzuführen.
```scala
def apply(f: FunctionValue, y: Int) : Int = f match {
  case F(n) => y + n
  case G(n) => y * n
}

def map(f: FunctionValue, l: List[Int]) : List[Int] = l match {
  case List() => List()
  case x::xs => apply(f,x)::map(f,xs)
}

def addAndMulNToList(n: Int, l: List[Int]) : List[Int] =
  map(G(n), map(F(n), l))
```

**Vorgehensweise:**
1. Lege abstrakte Oberklasse `FunctionValue` an mit Unterklassen für alle Funktionen
2. Lege `apply`-Funktion mit Fällen für alle Unterklassen von `FunctionValue` an, kopiere Rümpfe der Top-Level-Funktionen in die Fälle.
3. Ersetze Typ von Higher-Order-Parametern mit `FunctionValue`
4. Forme Aufrufe der Higher-Order-Funktionen mit `apply` um (`f(x)` wird zu `apply(f,x)`)


# Typsysteme
Ziel von Typsystemen ist es, bestimmte Arten semantischer Fehler (bspw. Addition von Funktionen oder Applikation einer Zahl) in einem syntaktisch korrekten Programm bereits vor dessen Ausführung zu erkennen. Ein Typsystem kann für die Fehler, die es erkennen soll, garantieren, dass diese bei keiner Ausführung eines Programms auftreten. Im Gegensatz dazu kann ein Test nur die fehlerfreie Ausführung eines Programms für eine bestimmte Eingabe garantieren.

Im Kontext von Typsystemen und Typecheckern spricht man von den Eigenschaften _Soundness_ und _Completeness_. 

:::info
Ein Typsystem ist _sound_, wenn es jeden bei der Ausführung auftretenden Typfehler vor der Ausführung meldet, und _complete_, wenn es nur Fehler meldet, die tatsächlich bei der Ausführung auftreten. Anders ausgedrückt: Einen Typecheck, der sound ist, bestehen **nur echt typsichere** Programme und einen Typecheck, der complete ist, bestehen **alle typsicheren** Programme. 

Im Fall von Soundness bestehen evtl. **echt typsichere** Programme den Typecheck nicht, im Fall von Completeness bestehen evtl. **nicht typsichere** Programme den Typecheck.
:::

Aus dem _Satz von Rice_ folgt, dass es für eine Turing-vollständige Sprache kein perfektes Typsystem, das Soundness und Completeness erfüllt, geben kann.

:::info
**Satz von Rice:** Sei $\mathcal{P}$ die Menge aller Turing-berechenbaren Funktionen und $\mathcal{S} \subsetneq \mathcal{P}$ eine nicht-leere, echte Teilmenge davon, so ist die Menge der Turingmaschinen, deren berechnete Funktion in $\mathcal{S}$ liegt, nicht entscheidbar.

Damit sind alle semantischen Eigenschaften von Programmen in Turing-vollständigen Sprachen nicht entscheidbar (d.h. es gibt keinen Algorithmus, der für jedes Programm entscheiden kann, ob die Eigenschaft zutrifft).
:::

Dies wird bereits an folgendem Beispiel deutlich:
```scala
f()
((x: Int) => x+1) + 3
```

Um entscheiden zu können, ob im obigen Programm ein Fehler durch die Addition einer Funktion und einer Zahl entsteht, müsste entschieden werden, ob `f` terminiert. Dazu müsste das Halteproblem entscheidbar sein. Da das Halteproblem unentscheidbar ist, ist durch Widerspruch bewiesen, dass kein perfektes Typsystem existieren kann.

Es sind aber durchaus Typsysteme möglich, die entweder Completeness oder Soundness erfüllen. Dabei ist Soundness meist die interessantere Eigenschaft, weil damit das Typsystem die Typsicherheit garantieren kann (was für Completeness nicht der Fall ist). Es werden jedoch manche Programme abgelehnt, die in Wahrheit ohne Typfehler ausgeführt werden könnten. Es handelt sich also um eine _konservative_ Abschätzung bzw. eine _Überapproximation_, da in manchen Fällen Programme "sicherheitshalber" abgelehnt werden, wenn die Typsicherheit nicht garantiert werden kann.

Für unsere Sprachen wird die Syntax jeweils durch eine _kontextfreie Grammatik_ in Form der Case Classes definiert. Typkorrektheit ist aber keine kontextfreie Eigenschaft, was etwa am folgenden Programm deutlich wird:
```scala
wth("x", Add("x",3))
```

Um hier feststellen zu können, ob `"x"` gebunden ist und ob es sich dabei um eine Zahl handelt, muss der Kontext betrachtet werden, in dem `"x"` auftritt. Typkorrektheit ist in diesem Fall also offensichtlich eine _kontextsensitive_ Eigenschaft und kann deshalb nicht direkt in der Grammatik der Sprache definiert werden. Dies gilt für die meisten Programmiersprachen.

Ein wichtiger Gesichtspunkt von Typsystemen ist auch deren Nachvollziehbarkeit für Programmierer, es muss verständlich sein, wann und warum Fehler erkannt werden, damit es möglich ist, diese zu berichtigen. Um diese Nachvollziehbarkeit zu gewährleisten sind Typchecker meist Kompositional, d.h. der Typ eines Ausdrucks ergibt sich durch die Typen seiner Unterausdrücke.

## Interpreter mit Typsystem
In unseren bisherigen Interpretern haben wir beim Auftreten von Typfehlern (bspw. Addition von zwei Funktionen) einen Laufzeitfehler geworfen, wie etwa hier im FAE-Interpreter:
```scala
case Add(l,r) => (eval(l,env),eval(r,env)) match {
  case (NumV(a),NumV(b)) => NumV(a+b)
  case _ => sys.error("Can only add numbers")
}
```

Nun wollen wir für die folgende Sprache ein Typsystem definieren, so dass vor der Auswertung eines Programms überprüft werden kann, ob dieses typsicher ist.
```scala
sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Bool(b: Boolean) extends Exp
case class Add(l: Exp, r: Exp) extends Exp
case class If(c: Exp, t: Exp, f: Exp) extends Exp

def eval(e: Exp) : Exp = e match {
  case Add(l,r) => (eval(l), eval(r)) match {
    case (Num(x),Num(y)) => Num(x+y)
    case _ => sys.error("Can only add numbers")
  }
  case If(c,t,f) => eval(c) match {
    case Bool(true) => eval(t)
    case Bool(false) => eval(f)
    case _ => sys.error("Condition must be a boolean")
  }
  case _ => e
}
```

Am Interpreter ist bereits erkennbar, welche Typfehler auftreten können: Eine Additionsoperation, bei der nicht beide Summanden Zahlen sind, oder ein `If`-Ausdruck, dessen Bedingung nicht zu einem `Bool` auswertet. Um diese Fehler zu erkennen, wollen wir Werte Klassifizieren und nach Typ unterscheiden. Wir wollen zwischen Zahlen und Booleans unterscheiden.

Wir definieren eine neue abstrakte Klasse `Type` mit den zwei konkreten Unterklassen `NumType` und `BoolType`. Diese entsprechen den zwei Wertetypen, die die Auswertung eines Ausdrucks ergeben kann.
```scala
sealed abstract class Type
case class BoolType() extends Type
case class NumType() extends Type
```

Um Typechecking auf Programmen bzw. Ausdrücken unserer Sprache durchzuführen, definieren wir eine Funktion `typeCheck`. Bei einem Typechecker handelt es sich um eine kompositionale Zuweisung von Typen zu Ausdrücken im Programm, der Typ eines Ausdrucks wird also durch seine Unterausdrücke bestimmt. Unsere `typeCheck`-Funktion ist dementsprechend strukturell rekursiv:
```scala
def typeCheck(e: Exp) : Type = e match {
  case Num(_) => NumType()
  case Bool(_) => BoolType()
  case Add(l,r) => (typeCheck(l),typeCheck(r)) match {
    case (NumType(),NumType()) => NumType()
    case _ => sys.error("Type error in Add")
  }
  case If(c,t,f) => (typeCheck(c),typeCheck(t),typeCheck(f)) match {
    case (BoolType(),tType,fType) =>
      if (tType == fType) tType else sys.error("Type error in If")
    case _ => sys.error("Type error in If")
  }
}
```

Der `Num`- und `Bool`-Fall sind trivial, hier ist offensichtlich um welchen Typ es sich handelt. Im `Add`-Fall muss `typeCheck` auf beiden Unterausdrücken `NumType()` ergeben, da in unserer Sprache nur Addition von Zahlen erfolgreich ausgewertet werden kann. Sind die Summanden nicht beide vom Typ `NumType()`, so geben wir eine Fehlermeldung aus. Im `If`-Fall ist klar, dass der Typechecker auf der Bedingung `BoolType` ergeben muss, der Typ des `If`-Ausdrucks selbst lässt sich aber nicht ohne weiteres feststellen. Je nachdem, welcher Zweig betreten wird, müsste entweder der Typ von `t` oder von `f` rekursiv bestimmt und ausgegeben werden.

Um zu entscheiden, welcher Zweig betreten wird, müsste die Bedingung ausgewertet werden. Würde man diese Auswertung im Typechecker durchführen, so verliert dieser aber gewissermaßen seinen Nutzen, denn wenn er bei Typfehlern in einer `If`-Bedingung die gleichen Laufzeitfehler liefert wie der Interpreter, so bietet der Typechecker keinen Vorteil gegenüber einer Ausführung des Programms. 

In einer komplexeren Sprache (z.B. der Turing-vollständigen FAE-Sprache) wäre es nicht möglich, die Bedingung ohne den Kontext des `If`-Ausdrucks auszuwerten, zudem könnte die Auswertung der Bedingung evtl. nicht terminieren, wodurch der Typechecker kein Ergebnis liefern würde. In jedem Fall verliert der Typechecker seinen Nutzen als Prüfmittel vor der eigentlichen Auswertung, sobald er Teile des Programms auswertet.

Aus diesen Gründen bleibt nur die Möglichkeit, den Typ beider Zweige zu bestimmen und auf Gleichheit zu prüfen, falls `t` und `f` den gleichen Typ besitzen, kann dieser ausgegeben werden. Dadurch wird aber bei gewissen Programmen ein Typfehler gefunden, obwohl diese fehlerfrei auswerten:
```scala
val ex1 = If(false,true,1)
val ex2 = Add(2, If(true,3,true))
```

Bei der Prüfung auf Gleichheit handelt es sich um eine konservative Abschätzung des Programmverhaltens -- Ausdrücke, in denen im `then`- und `else`-Zweig verschiedene Typen vorliegen, werden abgelehnt, da die Typsicherheit nicht gewährleistet werden kann.

Wir können unsere Definition von Soundness für Typechecking noch etwas verfeinern: Soundness bedeutet auch, dass der Typecheck den Typ des Auswertungsergebnisses korrekt vorhersagt.

:::info
**Soundness des Typsystems:**

Für alle `e: Exp`, `v: Exp` und `t: Type` gilt: 
Falls `typeCheck(e) == t`, so gilt `eval(e) == v` mit `typeCheck(v) == t` ++oder++ `eval(e)` führt zu einem Laufzeitfehler, der nicht vom Typsystem abgedeckt wird, ++oder++ `eval(e)` terminiert nicht. 
:::

## Simply-Typed Lambda Calculus (STLC)
Wir beginnen mit substitutionsbasierten Interpreter für das ungetypte Lambda-Kalkül (FAE), da ohne getrennte Werte (`Value`) und Closures die Implementation eines Typsystem deutlich einfacher möglich ist. Wir ergänzen Funktionen um eine Annotation des Parametertyps, die vom Interpreter ignoriert wird. Diese Sprache ist die einfachste Form des _Simply-Typed Lambda Calculus_ (_STLC_).

Zusätzlich fügen wir einige gängige Erweiterungen für des STLC hinzu, nämlich ein Sprachkonstrukt `JUnit`, Bindungen mit `Let` (ohne Typannotationen), Typ-Annotationen für beliebige Ausdrücke, Produkttypen (Tupel) und Summentypen (mit zwei Alternativen):
```scala
sealed abstract class Type

sealed abstract class Exp
case class Num(n: Int) extends Exp
case class Id(name: String) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Fun(param: String, t: Type, body: Exp) extends Exp
case class App (fun: Exp, arg: Exp) extends Exp
case class Junit() extends Exp
case class Let(x: String, xDef: Exp, body: Exp) extends Exp
case class TypeAscription(e: Exp, t: Type) extends Exp

case class Product(left: Exp, right: Exp) extends Exp
case class Fst(e: Exp) extends Exp
case class Snd(e: Exp) extends Exp

case class SumLeft(left: Exp, right: Type) extends Exp
case class SumRight(left: Type, right: Exp) extends Exp
case class EliminateSum(e: Exp, funLeft: Exp, funRight: Exp) extends Exp
```

Wir erweitern den [substitutionsbasierten FAE-Interpreter](https://pad.its-amazing.de/programmiersprachen1teil1#Substitutionsbasierter-Interpreter1) um die zusätzlichen Fälle, wobei wir auch die Hilfsfunktionen `freshName`, `freeVars` und `subst` erweitern müssen. 
```scala
def freeVars(e: Exp) : Set[String] =  e match {
  case Id(x) => Set(x)
  case Fun(p,_,b) => freeVars(b)-x
  // ...
  case Junit() => Set.empty
  case Let(x,xDef,b) => freeVars(xDef)++(freeVars(b) - x)
  case Product(l,r) => freeVars(l)++freeVars(r)
  case SumLeft(e,_) => freeVars(e)
  case SumRight(_,e) => freeVars(e)
  case EliminateSum(e,fl,fr) => freeVars(e)++freeVars(fl)++freeVars(fr)
}

def subst(e : Exp, x: String, xDef: Exp) : Exp = e match {
  case Id(y) => if (x == y) xDef else Id(y)
  // ...
  case Fun(param,t,body) =>
    if (param == x) e else {
      val fvs = freeVars(body) ++ freeVars(xDef)
      val newvar = freshName(fvs, param)
      Fun(newvar, t,subst(subst(body, param, Id(newvar)), x, xDef))
    }
  case Let(y,ydef,body) =>
    if (x == y) Let(y,subst(ydef,x,xDef),body) else {
      val fvs = freeVars(body) ++ freeVars(xDef)
      val newvar = freshName(fvs,y)
      Let(newvar,subst(ydef,x,xDef),subst(subst(body,y,Id(newvar)),x,xDef))
    }
  case Junit() => e
  // ...
}

def eval(e: Exp) : Exp = e match {
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => (eval(l), eval(r)) match {
    case (Num(x),Num(y)) => Num(x+y)
    case _ => sys.error("Can only add numbers")
  }
  case App(f,a) => eval(f) match {
    case Fun(x,_,body) => eval( subst(body,x, eval(a)))
    case _ => sys.error("Can only apply functions")
  }
  case TypeAscription(e,_) => eval(e)
  case Let(x,xdef,body) => eval(subst(body,x,eval(xdef)))
  case Product(a,b) => Product(eval(a),eval(b))
  case Fst(e) => eval(e) match {
    case Product(a,b) => a
    case _ => sys.error("Can only select first from products")
  }
  case Snd(e) => eval(e) match {
    case Product(a,b) => b
    case _ => sys.error("Can only select second from products")
  }
  case SumLeft(e,t) => SumLeft(eval(e),t)
  case SumRight(t,e) => SumRight(t,eval(e))
  case EliminateSum(e,fl,fr) => eval(e) match {
    case SumLeft(e2,_) => eval(App(fl,e2))
    case SumRight(_,e2) => eval(App(fr,e2))
    case _ => sys.error("Can only eliminate sums")
  }
  case _ => e // Num & Fun case
}
```

Wir legen ein Typsystem an, das Zahlen, Funktionen, JUnit, Produkttypen und Summentypen unterscheidet:
```scala
case class NumType() extends Type
case class FunType(from: Type, to: Type) extends Type
case class JunitType() extends Type
case class ProductType(left: Type, right: Type) extends Type
case class SumType(left: Type, right: Type) extends Type
```

`FunType()`, `ProductType()` und `SumType()` sind dabei rekursiv definiert und enthalten selbst jeweils zwei `Type`-Felder. Im Fall von Funktionen sind das der Parameter- und Ausgabetyp. Die Ergänzung der Annotation für den Argumenttyp bei Funktionen sorgt dafür, dass wir den `from`-Typ von Funktionen nicht "erraten" müssen. Der `to`-Typ kann unter Angabe des `from`-Typs problemlos durch Typechecking des Funktionsrumpfes bestimmt werden.

Um mit Identifiern umzugehen, benötigt unser Typechecker ein zweites Argument, nämlich eine Typumgebung (die meist mit $\Gamma$ oder als _Symbol Table_ bezeichnet wird), in der der Typ von Identifiern hinterlegt wird, damit er im `Id`-Fall ausgelesen werden kann.

```scala
def typeCheck(e: Exp, gamma: Map[String,Type]) : Type = e match {
  case Num(_) => NumType()
  case Id(x) => gamma.get(x) match {
    case Some(t) => t
    case _ => sys.error("Type error: Unbound identifier "+x)
  }
  case Add(l,r) => (typeCheck(l,gamma),typeCheck(r,gamma)) match {
    case (NumType(),NumType()) => NumType()
    case _ => sys.error("Type error: Can only add numbers")
  }
  case Fun(p,t,b) => FunType(t, typeCheck(b,gamma+(p -> t)))
  case App(f,a) => typeCheck(f,gamma) match {
    case FunType(from,to) =>
      if (from == typeCheck(a,gamma)) to
      else sys.error("Type error: Arg does not match expected type")
    case _ => sys.error("Type error: Left expression must be a function")
  }
  case JUnit() => JUnitType()
  case Let(x,xDef,b) => typeCheck(b,gamma+(x -> typeCheck(xDef,gamma)))
  case TypeAscription(e,t) =>
    if (typeCheck(e,gamma)==t) t
    else sys.error("Type error: Type does not match")
  case Product(l,r) => ProductType(typeCheck(l,gamma),typeCheck(r,gamma))
  case Fst(e) => typeCheck(e,gamma) match {
    case ProductType(l,_) => l
    case _ => sys.error("Type error: Can only project products")
  }
  case Snd(e) => typeCheck(e,gamma) match {
    case ProductType(_,r) => r
    case _ => sys.error("Type error: Can only project products")
  }
  case SumLeft(l,t) => SumType(typeCheck(l,gamma),t)
  case SumRight(t,r) => SumType(t,typeCheck(r,gamma))
  case EliminateSum(c,fl,fr) => typeCheck(c,gamma) match {
    case SumType(l,r) => (typeCheck(fl,gamma),typeCheck(fr,gamma)) match {
      case (FunType(lf,lt),FunType(rf,rt)) if l==lf && r==rf && lt==rt =>
        if (lTo==rTo) lTo
        else sys.error("Type error: Functions must have same return type")
      case _ => sys.error("Type error: 2nd and 3rd arg must be functions")
    }
    case _ => sys.error("Type error: Can only eliminate sums")
  }
}
```

:::info
**Soundness und Terminierung von STLC:**
Für `e: Exp` mit `typeCheck(e) == t` gilt: `eval(e)` terminiert und `typeCheck(eval(e),Map()) == t`.
:::

STLC ist nicht Turing-vollständig und in STLC können nur terminierende Programme verfasst werden. Aus diesem Grund wird STLC bei der Implementation von Programmiersprachen häufig auf Typ-Level verwendet, da man bspw. Typfunktionen und deren Applikation formulieren will, aber nicht-terminierende Ausdrücke auf Typ-Level verhindern muss, da sonst der Typechecker nicht mehr zwingend terminiert.


# Hindley-Milner-Typinferenz
Zuletzt wollen wir noch ein Typsystem betrachten, das ohne jegliche Typannotationen auskommt und bei dem Typen _inferiert_ werden können. Dazu wird beim Typechecking eine Liste von _Constraints_ erzeugt und ähnlich wie bei einem linearen Gleichungssystem nach einer Belegung mit Typen gesucht, die alle Constraints erfüllt.

Wir kehren dazu zum nicht-erweiterten STLC zurück, wobei wir den substitutionsbasierten Interpreter verwenden. Wir unterscheiden die Typen `FunType()` und `NumType()`, außerdem ergänzen wir Typvariablen.

```scala
sealed abstract class Type
case class FunType(from: Type, to: Type) extends Type
case class NumType() extends Type
case class TypeVar(x: String) extends Type
```
Da der Typ von Identifiern (d.h. Funktionsparametern) erst bei deren Verwendung bestimmt werden kann, wird Identifiern bei ihrem ersten Auftreten eine Typvariable zugeordnet. Bei der Auswertung von Ausdrücken, in dem ein Identifier genutzt wird, können dann Constraints für die Typvariable erzeugt werden -- wird ein Identifier bspw. mit einer Zahl addiert, so muss er den Typ `NumType()` besitzen. 

Bei den Constraints handelt es sich um Typgleichungen, bei denen der linke und rechte Typ übereinstimmen müssen. Diese werden als Tupel der Form `(Type,Type)` repräsentiert. Die Ausgabe von `typeCheck` ist eine Liste solcher Constraint-Tupel zusammen mit dem Typen des Ausdrucks `e`, bei dem es sich auch um eine Typvariable handeln kann. 

Um Identifier mit ihrer jeweiligen Typvariable zu assoziieren, verwenden wir wieder eine Typumgebung `gamma`.
```scala
var typeVarCount: Int = 0
def freshTypeVar() : Type = {
  typeVarCount += 1
  TypeVar("X"+typeVarCount.toString)
}

def typeCheck(e: Exp, g: Map[String,Type]) : (List[(Type,Type)],Type) = e match {
  case Num(_) => (List(),NumType())
  case Id(x) => g.get(x) match {
    case Some(t) => (List(),t)
    case _ => sys.error("Unbound identifier: "+x)
  }
  case Add(l,r) => (typeCheck(l,g),typeCheck(r,g)) match {
    case ((lEqs,lt),(rEqs,rt)) => 
      (lt->NumType() :: rt->NumType() :: lEqs ++ rEqs, NumType())
  }
  case Fun(p,b) =>
    val xt = freshTypeVar()
    val resBody = typeCheck(b,g+(p->xt))
    (resBody._1, FunType(xt,resBody._2))
  case App(f,a) =>
    val toType = freshTypeVar()
    (typeCheck(f,g),typeCheck(a,g)) match {
      case ((fEqs, ft), (aEqs, at)) => 
        ((ft, FunType(at, toType)) :: fEqs ++ aEqs, toType)
    }
}
```

Im `Num`-Fall wird eine leere Constraint-Liste und der Typ `NumType()` zurückgegeben. Im `Id`-Fall wird der Identifier `x` in der Typumgebung nachgeschlagen, wird ein Eintrag gefunden, so wird der entsprechende Typ und eine leere Constraint-Liste ausgegeben, ansonsten wird ein Fehler geworfen.

Im `Add`-Fall werden erst Typechecking auf den Unterausdrücken durchgeführt, die Constraint-Liste wird um zwei Gleichungen erweitert: Der Typ beider Unterausdrücke muss mit `NumType()` übereinstimmen, da nur Zahlen addiert werden können. Es wird der Typ `NumType()` ausgegeben.

Im `Fun`-Fall wird eine "frische" Typvariable für den `param`-Identifier erzeugt, `typeCheck` wird rekursiv auf dem Rumpf aufgerufen, wobei in der Typumgebung der Parameter an die neue Typvariable gebunden wird. Das Typechecking des Rumpfes ergibt die Constraint-Liste und den `to`-Typ der Funktion, für den `from`-Typ wird die neue Typvariable eingesetzt.

Im `App`-Fall wird eine neue Typvariable `toType` für das Ergebnis der Funktionsapplikation erzeugt, anschließend wird rekursiv Typechecking auf beiden Unterausdrücken durchgeführt. Die Constraint-Liste wird erweitert um die Bedingung, dass der Typ der Funktion `FunType()` ist, wobei der `from`-Typ mit dem Typ des Arguments und der `to`-Typ mit `toType` übereinstimmt. Es wird `toType` ausgegeben.

## Unifikationsalgorithmus von Robinson
Die `typeCheck`-Funktion trifft im Gegensatz zu der im [STLC-Typsystem](#Simply-Typed-Lambda-Calculus-STLC) (bis auf das Erkennen ungebundener Identifier) noch gar keine Aussage darüber, ob ein Ausdruck typkorrekt ist. Dazu muss für die von `typeCheck` ausgegebene Liste von Typgleichungen geprüft werden, ob sich alle Gleichungen _unifizieren_ lassen. Dazu verwenden wir den Unifikationsalgorithmus nach [Robinson](https://de.wikipedia.org/wiki/John_Alan_Robinson):
```scala
def substitution(x: String, s: Type): Type => Type = new Function[Type,Type] {
  def apply(t: Type) : Type = t match {
    case FunType(from, to) => FunType(this(from), this(to))
    case NumType() => NumType()
    case TypeVar(y) => if (x==y) s else t
  }
}

def freeTypeVars(t: Type) : Set[String] = t match {
  case FunType(f,t) => freeTypeVars(f)++freeTypeVars(t)
  case NumType() => Set()
  case TypeVar(x) => Set(x)
}

def unify(eq: List[(Type,Type)]) : Type => Type = eq match {
  case List() => identity
  case (NumType(),NumType())::rest => unify(rest)
  case (FunType(f1,t1),FunType(f2,t2))::rest => unify(f1->f2::t1->t2::rest)
  case (TypeVar(x1),TypeVar(x2))::rest if x1==x2 => unify(rest)
  case (TypeVar(x),t)::rest =>
    if (freeTypeVars(t)(x)) sys.error(s"Occurs check: $x occurs in $t")
    val s = substitution(x,t)
    s.andThen(unify(rest.map(tup => (s(tup._1),s(tup._2)))))
  case (t,TypeVar(x))::rest => unify((TypeVar(x),t)::rest)
  case (t1,t2)::_ => sys.error(s"Cannot unify $t1 and $t2")
}
```

`unify` bestimmt die einfachste Belegung der Typvariablen, die alle durch `typeCheck` generierten Gleichungen erfüllt, und gibt eine Funktion aus, die alle Typvariablen entsprechend dieser Belegung substituiert. 

Stimmen im ersten Tupel der Liste von Gleichungen die linke und rechte Seite bereits überein, so wird `unify` einfach auf der Restliste aufgerufen. Steht links und rechts der Typ `FunType()`, so wird die Liste um zwei Bedingungen erweitert, dass der `from`- und `to`-Typ jeweils übereinstimmen (_generative Rekursion_).

Steht im ersten Tupel links eine Typvariable und rechts ein anderer Typ, so wird mittels `freeTypeVars` geprüft, ob die Typvariable innerhalb des anderen Typs auftritt. Ist dies der Fall, so ist die Gleichung "selbstbezüglich" und kann unmöglich erfüllt werden (bspw. gibt es keinen Typ `X`, für den `X` und `FunType(X,X)` gleichbedeutend sind). In diesem Fall wird also ein Fehler geworfen.

Andernfalls wird mit `substitution` eine Funktion angelegt, die alle Vorkommen der (linken) Typvariable durch den anderen (rechten) Typ ersetzt. Mit `andThen` wird eine Komposition der Substitution nach der Ergebnisfunktion des rekursiven Aufrufs von `unify` angelegt, wobei `unify` auf der Restliste aufgerufen wird, in der die Substitution mit `map` auf alle Tupel angewendet wurde.

Steht die Typvariable rechts und ein anderer Typ links, so werden die Einträge im Tupel vertauscht, in allen anderen Fällen können die zwei Typen im Tupel nicht unifiziert werden und es wird ein Fehler geworfen.

Erreicht `unify` das Ende der Liste, so wird die Identitätsfunktion ausgegeben, durch die Komposition der Substitutionsfunktionen gibt `unify` also eine Funktion aus, die die einen Typ akzeptiert darin der unifizierenden Belegung entsprechend alle Typvariablen substituiert. Es kann also der gesamte Typecheck mit der folgenden Funktion durchgeführt werden:
```scala
def doTypeCheck(e: Exp): Type = {
  val (eqs,t) = typeCheck(e,Map())
  unify(eqs)(t)
}
```

:::info
**Completeness der Typinferenz:**
Gibt es für ein Programm Typannotationen, mit denen es vom STLC-Typechecker akzeptiert wird, so wird akzeptiert der Typecheck mit Typinferenz auch die nicht-typannotierte Variante des Programms.

**Soundness:**
Für alle `e: Exp` gilt:
Falls `doTypeCheck(e) == t`, dann gilt `eval(e) == v` mit `doTypeCheck(v) == t` (unter $\alpha$-Konversion, d.h. Umbennenung der Typvariablen ohne Veränderung der Bedeutung).
:::

## Let-Polymorphismus
Mit der bisherigen Definition der Sprache und des Typsystems ist es nicht möglich, _polymorphe_ Funktionen auszudrücken, die für mehrere Argumenttypen genutzt werden können. Bspw. müsste man die Identitätsfunktion oder die Kompositionsfunktion für jeden Eingabetyp getrennt anlegen.

Wir ermöglichen polymorphe Funktionen durch lokale Bindungen mit `Let`:
```scala
case class Let(x: String, xDef: Exp, body: Exp) extends Exp
```

In `typeCheck` ergänzen wir den folgenden Fall für `Let`:
```scala
def typeCheck(e: Exp, g: Map[String,Type]) : (List[(Type,Type)],Type) = e match {
  // ...
  case Let(x,xDef,b) =>
    val (eqs1,_) = typeCheck(xDef,g)
    val (eqs2,t) = typeCheck(subst(b,x,xDef),g)
    (eqs1++eqs2,t)
}
```

Erst wird `typeCheck` rekursiv auf `xDef` aufgerufen, anschließend rufen wir `typeCheck` rekursiv auf dem Rumpf des `Let`-Ausdrucks auf. Dabei verwenden wir aber (innerhalb von `typeCheck`!) die `subst`-Funktion des Interpreters, um alle Vorkommen von `x` im Rumpf durch `xDef` zu ersetzen. Es werden die Constraints von `typeCheck` auf `xDef` und auf dem Rumpf konkateniert und mit dem Ergebnistyp von `typeCheck` auf dem Rumpf ausgegeben.

Durch die Substitution vor der Durchführung des Typechecks wird jedes Vorkommen von `x` unabhängig von den anderen Vorkommen auf Typkorrektheit geprüft und die Typen der Vorkommen müssen nicht zwingend übereinstimmen. 

Bspw. würde also beim Typechecking des Ausdrucks `Let("id",Fun("x","x"),...)` das Typechecking für jedes Vorkommen der Identitätsfunktion unabhängig voneinander durchgeführt werden. Dadurch kann die Funktion polymorph für jeden beliebigen Argumenttyp genutzt werden, da für alle Vorkommen jeweils der korrekte Typ (etwa `FunType(NumType(),NumType())`) inferiert wird.








:::success
- [ ] Mark & Sweep fertig zusammenfassen (???)
- [ ] Alle Vorlesungen bis 16.10.
- [ ] 2 Wochen für Lecture Notes, Wiederholungen, Lernen, Probeklausur, evtl. Altklausuren
:::
