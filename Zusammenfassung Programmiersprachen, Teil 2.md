---
title: Zusammenfassung Programmiersprachen, Teil 2
description: Programmiersprachen 1, SoSe 2020, Klauso3
langs: de

---

# Inhaltsverzeichnis

[TOC]


# Webprogrammierung und Continuations
Angenommen, man will eine interaktive Webanwendung über mehrere Webseiten hinweg schreiben, so wird man vor eine Herausforderung gestellt: Das Webprotokoll HTTP ist zustandslos, d.h. Anfragen sind unabhängig voneinander und es ist kein Zugriff auf vorherige Anfragen und den dabei übermittelten Daten möglich. Betrachten wir etwa die interaktive Funktion `progSimple` im folgenden Code:
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

Hier finden nacheinander zwei Eingaben durch den Nutzer statt, wobei beide Werte relevant für das Ergebnis sind. Würde man diese Funktion im Web umsetzen wollen, so dass der Nutzer auf zwei verschiedenen Seiten die Werte eingibt und absendet und auf einer dritten Seite das Ergebnis angezeigt bekommt, dann ist aufgrund der Zustandslosigkeit von HTTP ein spezieller Programmierstil notwendig. 

Der Ablauf zerfällt dabei in die folgenden Teilprogramme:
- **Teilprogramm $a$** generiert das Formular für die erste Zahl
- **Teilprogramm $b$** konsumiert die Zahl aus dem ersten Formular und generiert das zweite Formular
- **Teilprogramm $c$** konsumiert die Daten aus $b$, berechnet die Ausgabe und erzeugt die Seite mit dem Ergebnis.

Nun stellt sich die Frage, wie im zustandslosen Protokoll das Teilprogramm $c$ auf die in $a$ eingegebenen Daten zugreifen kann. Hierzu muss der eingegebene Wert über $b$ weitergereicht werden, etwa als verstecktes Formularfeld in HTML oder Parameter in der URL. 

Wir können die Zustandslosigkeit mit dem Rückgabetyp `Nothing` modellieren, dabei brechen alle Funktionen mit einem Fehler die Berechnung ab und besitzen keinen Rückgabewert.
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

Hier wird die Weitergabe der Daten von einem Teilprogramm zum nächsten nur durch die Ausgaben und Eingaben in der Konsole durch den Nutzer modelliert, d.h. es wird bspw. erst `progA` mit `2` aufgerufen, dann `progB` mit `3` und zuletzt `progC` mit `2` und `3`.

Wir können aber auch die jeweils noch notwendigen Schritte als Programm repräsentiert werden, hierbei spricht man von _Continuations_. Im Fall von `progA` muss anschließend noch die zweite Zahl eingelesen, beide Zahlen addiert und das Ergebnis ausgegeben werden.

```scala
val continuations = new mutable.HashMap[String, Int=>Nothing]
var nextIndex : Int = 0
def getNextId : String = {
  nextIndex += 1
  "c"+nextIndex
}

def webReadCont(prompt: String, k: Int => Nothing) : Nothing = {
  val id = getNextId
  continuations += (id -> k)
  println(prompt)
  println("To continue, invoke continuation "+id)
  sys.error("Program terminated")
}

def webDisplay(s: String) : Nothing = {
  println(s)
  sys.error("Program terminated")
}

def continue(kId: String, result: Int): Nothing = continuations(kId)(result)

def webProg =
  webReadCont("First number:", (n: Int) =>
    webReadCont("Second number:", (m: Int) =>
      webDisplay("Sum of "+n+" and "+m+" is "+(n+m))))
```

Nun kann zuerst `webProg` aufgerufen werden, wobei der Name der nächsten Continuation ausgegeben wird. `continue` kann dann mit dieser Continuation und der ersten Zahl aufgerufen werden, die dabei ausgegebene Continuation kann dann `continue` mit der zweiten Zahl übergeben werden, woraufhin das Ergebnis angezeigt wird.

In Bezug auf Webprogrammierung entsprechen die Continuations Auswertungszuständen, die hinterlegt werden, wobei der Client eine ID erhält, um diesen Zustand mit der nächsten Eingabe aufzurufen. Somit könnte auch beim Klonen des Tabs oder bei Verwendung des "Zurück"-Buttons in allen Instanzen das Programm korrekt fortgesetzt werden. Das steht im Kontrast zu einer Implementierung mit einer _Session_, wobei der Client (und nicht der Zustand) anhand einer übermittelten ID "erkannt" wird. In diesem Fall könnte es bei mehreren Instanzen zu fehlerhaften Ergebnissen kommen, da diese nicht unabhängig sind.

Betrachten wir nun den allgemeineren Fall der n-fachen Addition. Im folgenden Programm wird eine Liste von Gegenständen rekursiv durchlaufen, wobei der Nutzer für jeden Gegenstand aufgefordert wird, einen Preis einzugeben. Nachdem alle Listenelemente abgearbeitet wurden, wird die Summe der Zahlen ausgegeben.

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
def addAllCostsCont(itemList: List[String], k: Int => Nothing) : Nothing = {
  itemList match {
    case List() => k(0)
    case first :: rest =>
      webReadCont("Cost of "+first+":",
        (n: Int) => addAllCostsCont(rest, (m: Int) => k(m+n)))
  }
}

def testWeb() : Unit = addAllCostsCont(testList, m => webDisplay("Total cost: "+m))
```











:::success
- [ ] VL 12
- [ ] Mark & Sweep fertig zusammenfassen (???)
- [ ] Fixpunkt-Kombinator
- [ ] PP
- [ ] Alle Vorlesungen bis 14. 10. - 2 Wochen für Lecture Notes, Wiederholungen, Lernen, Probeklausur, evtl. Altklausuren
:::
