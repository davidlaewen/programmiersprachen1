---
title: Zusammenfassung Programmiersprachen, Teil 2
description: Programmiersprachen 1, SoSe 2020, Klauso3
langs: de

---

# Inhaltsverzeichnis

[TOC]


# Webprogrammierung mit Continuations
Angenommen, man will eine interaktive Webanwendung über mehrere Webseiten hinweg programmieren, so wird man vor eine Herausforderung gestellt: Das Webprotokoll HTTP ist zustandslos, d.h. Anfragen sind unabhängig voneinander und es ist kein Zugriff auf vorherige Anfragen und dabei übermittelte Daten möglich. Betrachten wir etwa die interaktive Funktion `progSimple` im folgenden Code:
```scala
import scala.io.StdIn.readLine

def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def progSimple(): Unit = {
  println(inputNumber("Enter first number:") + inputNumber("Enter second number:"))
} 
```

Hier finden nacheinander zwei Eingaben durch den Nutzer statt, wobei das Ergebnis aus beiden Werten berechnet wird. Würde man diese Funktion im Web umsetzen wollen, so dass der Nutzer auf zwei verschiedenen Seiten die Werte eingibt und absendet und auf einer dritten Seite das Ergebnis angezeigt bekommt, dann ist aufgrund der Zustandslosigkeit von HTTP ein spezieller Programmierstil notwendig. 

Der Ablauf zerfällt dabei in die folgenden Teilprogramme:
- **Teilprogramm $a$** generiert das Formular für die erste Zahl.
- **Teilprogramm $b$** konsumiert die Zahl aus dem ersten Formular und generiert das Formular für die zweite Zahl.
- **Teilprogramm $c$** konsumiert die Daten aus $b$, berechnet die Ausgabe und erzeugt die Seite mit dem Ergebnis.

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

Hier wird die Weitergabe der Daten von einem Teilprogramm zum nächsten nur durch die Ausgaben und Eingaben in der Konsole durch den Nutzer modelliert, d.h. es wird bspw. erst `progA` mit `2` aufgerufen, dann `progB` mit `3` und zuletzt `progC` mit `2` und `3`. Hier ist der Nutzen der durchgeführten Programmtransformation noch nicht sonderlich klar erkenntbar.

Es können aber auch die jeweils noch notwendigen Schritte als _Continuation_ repräsentiert werden, im Fall von `progA` muss etwa noch die zweite Zahl eingelesen werden, dann müssen beide Zahlen addiert und das Ergebnis ausgegeben werden.

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

Nun kann zuerst `webProg` aufgerufen werden, es wird die nächste Continuation ausgegeben. Mit dieser Continuation und der ersten Zahl kann dann `continue` aufgerufen werden, die dabei ausgegebene Continuation kann dann `continue` mit der zweiten Zahl übergeben werden, woraufhin das Ergebnis angezeigt wird.

In Bezug auf Webprogrammierung entsprechen die Continuations Auswertungszuständen, die hinterlegt werden, wobei der Client einen Bezeichner erhält, um diesen Zustand mit der nächsten Eingabe aufzurufen. Somit könnte auch beim Klonen des Tabs oder bei Verwendung des "Zurück"-Buttons das Programm in allen Instanzen korrekt fortgesetzt werden. Das steht im Kontrast zu einer Implementierung mit einer _Session_, wobei der Client (und nicht der Zustand) anhand einer übermittelten Session-ID erkannt wird. In diesem Fall könnte es bei mehreren Instanzen zu fehlerhaften Ergebnissen kommen, da diese nicht unabhängig sind.

Betrachten wir nun den allgemeineren Fall der n-fachen Addition: Im folgenden Programm wird eine Liste von Gegenständen rekursiv durchlaufen, wobei der Nutzer für jeden Gegenstand aufgefordert wird, einen Preis einzugeben. Nachdem alle Listenelemente abgearbeitet wurden, wird die Summe der Zahlen ausgegeben.

```scala
def inputNumber(prompt: String) : Int = {
  println(prompt)
  Integer.parseInt(readLine())
}

def addAllCosts(items: List[String]): Int = items match {
  case List() => 0
  case first :: rest => inputNumber("Cost of "+first+":") + addAllCosts(rest)
}

val testList = List("Banana", "Apple", "Orange")
def test() : Unit = println("Total cost: " + addAllCosts(testList))
```

Dieses Programm besitzt nach Anpassung an den "Web-Stil" die Form:
```scala
def addAllCosts_k(itemList: List[String], k: Int => Nothing) : Nothing = {
  itemList match {
    case List() => k(0)
    case first :: rest =>
      webRead_k("Cost of "+first+":",
        (n: Int) => addAllCosts_k(rest, (m: Int) => k(m+n)))
  }
}

def testWeb() : Unit = addAllCosts_k(testList, m => webDisplay("Total cost: "+m))
```

Die Funktion `addAllCosts_k` wird aufgerufen mit der Liste von Gegenständen und der Contination `m => webDisplay("Total cost: "+m)`. Im Fall der leeren Liste wird die Continuation `k` auf `0` aufgerufen, es würde also `Total cost: 0` angezeigt werden. Ist die Liste nicht leer, so wird durch `webRead_k` der Nutzer dazu aufgefordert, den Preis des ersten Gegenstandes einzugegeben. Dabei wird die Continuation `n => addAllCosts_k(rest, m => k(m+n))` übergeben, diese Continuation wird also vom Nutzer/Client als nächstes mit dem entsprechenden Preis aufgerufen. Dabei wird die Continuation `m => k(m+n)` an `addAllCosts_k` zurückgereicht (wobei `n` durch Scalas interne Closures an die Eingabe gebunden ist), d.h. die Nutzereingabe `n` wird jeweils den Kosten hinzugefügt.

Die Funktion `addAllCosts` lässt sich geschickt mit Map umformulieren:
```scala
def addAllCostsMap(items: List[String]) : Int = {
  items.map((s: String) => inputNumber("Cost of " + s + ":")).sum
}
```

Würden wir auf dieser Implementation die Web-Transformation anwenden wollen, so müssten wir auch Map transformieren. Die Web-Transformation ist also "allumfassend" und betrifft alle Funktionen, die in einem Programm auftreten (bis auf primitive Operationen). Wir müssen in diesem Fall also `map` im Web-Stil verfassen. Oben ist die "normale" Implementation von `map`, unten die Web-transformierte Variante:
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

Bei der Transformation wird überall der Rückgabetyp mit `Nothing` ersetzt, die vorherigen Rückgabewerte werden stattdessen an entsprechende Continuations gereicht. So wird `f` nun durch ein neues Argument mit dem Typ `T => Nothing` ergänzt, der Typ der Eingabe entspricht dem ursprünglichen Rückgabetyp. Die `map`-Funktion selbst wird durch ein neues Argument mit dem Typ `List[T] => Nothing` ergänzt, auch hier entspricht der Typ der Eingabe dem ursprünglichen Rückgabetyp.

Die Auswertungsreihenfolge ist entscheidend für die Web-Transformation, die implizite Auswertungsreihenfolge der verwendeten Sprache (etwa bei geschachtelten Ausdrücken) muss ausformuliert werden, das Programm wird _sequentalisiert_. Die Transformation ist global, es müssen alle verwendet Funktionen (auch in Bibliotheken etc.) transformiert werden. 

Aufgrund des Aspekts der Sequentialisierung ist die Transformation mit Continuations auch für das Programmieren von Compilern relevant. Man spricht bei diesem "Web-Stil" auch von _Continuation Passing Style_ (_CPS_).

# Continuation Passing Style
Programme in CPS besitzen die folgenden Eigenschaften:
- Alle Zwischenwerte besitzen einen Namen.
- Die Funktionsanwendungen werden sequentialisiert (und die Auswertungsreihenfolge ist somit explizit).
- Alle Ausdrücke erhalten einen Continuation-Parameter und liefern keinen Rückgabewert (Rückgabetyp `Nothing`), sondern rufen den Continuation-Parameter auf dem Ergebnis auf.
- Alle Aufrufe sind _Tail Calls_.

:::info
Man spricht von einem **Tail Call**, wenn bei einem rekursiven Aufruf im Rumpf einer Funktion keine weitere Berechnung nach der Rückkehr des Aufrufs stattfindet.
Der Aufruf `f(n+1)` ist in `def f(n: Int): Int = f(n+1)` ein Tail Call, in `def f(n: Int): Int = f(n+1)*2` jedoch nicht.

Liegt nach der CPS-Umwandlung eine "triviale" Continuation (d.h. `k` bleibt unverändert) bei einem rekursiven Aufruf vor, so handelt es sich um einen Tail Call.

Bei Rekursion mit Tail Call (_Endrekursion_) muss der Kontext des rekursiven Aufrufs nicht auf dem Call Stack gespeichert werden, in Scala wird deshalb einfache Endrekursion erkannt und entsprechend optimiert. In Java werden auch endrekursive Aufrufe auf dem Stack hinterlegt, weshalb Schleifen bzgl. des Speicherverbrauchs vorzuziehen sind. In Racket wird bei keiner Form von Endrekursion der Stack unnötig gefüllt.
:::

Bei der CPS-Transformation von Funktionen und Werten sind die folgenden Schritte notwendig:
1. Ersetzen aller Rückgabetypen durch `Nothing`, wobei auch der Typ von Konstanten durch Nothing ersetzt wird

2. Ergänzen eines Parameters `k` mit Typ `R => Nothing`, wobei `R` der ursprüngliche Rückgabetyp ist (Konstanten der Form `c: T` werden also in Funktionen der Form `c_k(k: T => Nothing): Nothing` umgewandelt)

3. Weitergabe des Ergebnisses an `k`, bei Konstante `val c: T = x` etwa `def c_k(k: T => Nothing): Nothing = k(x)`

4. Sequentialisierung durch Weiterreichen der Zwischenergebnisse, aus `f(f(42))` wird bspw. `f_k(42, n => f_k(n,k))` oder aus `f(1) + g(2)` wird `f_k(1, n => g_k(2, m => k(n+m)))`

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











:::success
- [ ] VL 13
- [ ] Mark & Sweep fertig zusammenfassen (???)
- [ ] Fixpunkt-Kombinator
- [ ] PP
- [ ] Alle Vorlesungen bis 14. 10. - 2 Wochen für Lecture Notes, Wiederholungen, Lernen, Probeklausur, evtl. Altklausuren
:::
