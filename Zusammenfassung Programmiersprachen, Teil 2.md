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

Eine Continuation ist eine Repräsentation des Call-Stacks als Funktion. 

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


# CPS-Interpreter
- **Funktionsapplikationen:** Aus `f(x)` mit `f: X => Y` wird `(k: Y => ...) => f_k(x, y => k(y))`

- **Konstanten:** Aus `c` wird `k => k(c)`

- **Funktionsdefinitionen:** Aus `x => y` wird `k => k( (x, dynK) => dynK(x) )`


# First-Class Continuations
In Programmiersprachen mit _First-Class Continuations_ (bspw. Scheme, Racket) gibt es Sprachkonstrukte, um die aktuelle Continuation zu jedem Zeitpunkt abzugreifen und um damit zu arbeiten (also um die Continuation zu reifizieren, zu binden, als Parameter zu übergeben oder aufzurufen). Mit solch einem Sprachfeature kann der Programmier bspw. fortgeschrittene Kontrollstrukturen selbst definieren.

In Racket gibt es die Funktion `let/cc`, mit dieser kann die aktuelle Continuation an einen Identifier gebunden und im Rumpf von `let/cc` aufgerufen werden.
```scheme
[>] (number->string (+ 1 (let/cc k (string-length (k 3)))))
"4"
```

Im obigen Beispiel werden die Funktionsaufrufe vor dem Aufruf von `let/cc` als Continuation an `k` gebunden, im Rumpf von `let/cc` wird dann `k` mit `3` aufgerufen und damit die Continuation fortgesetzt, es werden die in der Contination gespeicherten Funktionsaufrufe angewendet und `"4"` ausgegeben. Der Aufruf von `k` kehrt nicht zurück, wodurch der Funktionsaufruf von `string-length` zwischen `let/cc` und `k` nicht mehr auf das Ergebnis angewendet wird. 

Die Continuation kann auch durch `set!` an einen globalen Identifier gebunden werden, um sie außerhalb des Rumpfs von `let/cc` aufrufen zu können:
```scheme
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

In einem Programm, das bereits in CPS vorliegt, wäre das Bestimmen der aktuellen Continuation trivial. Für unsere Implementation von `LetCC` wollen wir aber nicht alle Programme transformieren, sondern stattdessen den Interpreter selbst in CPS verfassen. Bei einer Implementierung durch die automatische CPS-Transformation von Programmen müsste jedes Programm erst transformiert werden. Der Interpreters muss hingegen nur ein Mal transformiert werden. Der erste Schritt zur Implementierung von `LetCC` ist also das Transformieren des Interpreters.

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

Wir ersetzen den Rückgabetypen mit `Nothing` und ergänzen einen Continuation-Parameter `k` mit dem Typ `Value => Nothing`. Der `Num`-, `Id`- und `Fun`-Fall sind trivial, da diese nicht-rekursiv sind, wir reichen das Ergebnis einfach an `k` weiter. Im `Add`- und `App`-Fall muss die Auswertung des linken und rechten Unterausdrucks sequentialisiert werden, wir werten von links nach rechts aus.
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

Es wird `eval` eine Continuation überreicht, die das Ergebnis der Auswertung bindet und dann mit einem Fehler die Auswertung beendet (Rückgabetyp `Nothing`). Die Auswertung selbst wird in einem Try-Catch-Block gestartet, um den Fehler abzufangen. Das durch die Continuation gebundene Ergebnis kann von `startEval` ausgegeben werden.

## Implementierung von 'LetCC'
Jetzt wo der Interpreter selbst CPS-transformiert ist, können wir das neue Sprachkonstrukt `LetCC` mit wenig Aufwand ergänzen, da die Continuations auf Interpreter-Ebene  den Continuations in unserer Sprache sehr nahe sind. Zuerst erweitern wir `Exp` um das neue Sprachkonstrukt:
```scala
case class LetCC(param: String, body: Exp) extends Exp
```

Im Interpreter ergänzen wir den `LetCC`-Fall, hier setzen wir die Auswertung rekursiv im Body fort, wobei wir den Parameter an die aktuelle Continuation `k` binden. Da `k` den Typ `Value => Nothing`, die Umgebung aber den Typ `Map[String,Value]` besitzt, können wir die Continuation nicht direkt in der Umgebung binden. Stattdessen erweitern wir `Value` um den Fall `ContV`:
```scala
case class ContV(k: Value => Nothing) extends Value
```

Nun sind Continations eine Werte-Art und können wie andere Werte an Identifier gebunden werden. Es fehlt noch die Anwendung einer Continuation auf ein Argument, hierzu überladen wir das `App`-Konstrukt, so dass damit Funktionen und Continuations angewendet werden können. Im `App`-Fall ergänzen wir einen Fall für `ContV` neben dem Fall für `ClosureV`:
```scala
case App(f,a) => eval(f, env, fv => fv match {
  case ClosureV(Fun(p,b),cEnv) =>
    eval(a, env, av => eval(b, cEnv+(p -> av), k))
  case ContV(k2) => eval(a, env, av => k2(av))
  case _ => sys.error("Can only apply functions")
})
```

Im `ContV`-Zweig werten wir das Argument aus und übergeben dabei eine Continuation, die das Ergebnis an `av` bindet und dann der aufzurufenden Continuation `k2` aus dem `ContV`-Objekt `av` überreicht. Dabei wird die aktuelle Continuation `k` ignoriert, die Auswertung "springt" ohne zurückzukehren. Der Wechsel zur Continuation `k2` entspricht quasi einem Austauschen des Call-Stacks.

Durch das "Wrappen" der Interpreter-Continuations konnten wir diese also in Continuations für unsere Sprache umwandeln.


# Delimited Continuations
Die (_Undelimited_) Continuations, die wir bisher kennen gelernt haben, führen bei einem Aufruf zu einem "Sprung", wobei die Auswertung nicht mehr zur Stelle des Aufrufs zurückkehrt. Somit ist keine Komposition (d.h. "Nacheinanderschalten") von Continuations möglich, sie können also nicht kombiniert werden.

Dies ist mit _Delimited Continuation_ möglich, sie repräsentieren nur einen Ausschnitt des Call-Stacks (während _Undelimited Continuations_ den gesamten Call-Stack repräsentieren) und kehren nach ihrem Aufruf zurück. Aus diesem Grund werden Delimited Continuations auch als _Composable Continuations_ bezeichnet.

In der Racket-Bibliothek `racket/control` gibt es die zwei Funktionen `shift` und `reset`, mit denen Delimited Continuations erzeugt werden können. Dabei verhält sich `shift` ähnlich wie `let/cc`, `shift` bindet aber nur die Continuation ab dem nächstliegenden Aufruf von `reset`, die Continuation wird also durch `reset` begrenzt (_delimitiert_) und repräsentiert nur den Call-Stack bis zum nächsten Aufruf von `reset`.
```scheme
(* 2 (reset (+ 1 (shift k (k 5)))))
```

Im obigen Beispiel wird die Continuation, die durch `shift` an `k` gebunden wird, durch `reset` beschränkt auf den Aufruf von `+` mit `1`. Wird `k` auf `5` aufgerufen, so wird nur die Addition auf `5` durchgeführt, die Multiplikation vor `reset` gehört nicht zur Continuation. Somit entspricht die Berechnung `(* 2 (+ 1 5))` und das Ergebnis ist `12`. Da es sich bei `k` um eine Delimited Continuation handelt, ist auch Continuation-Komposition möglich:
```scheme
(* 2 (reset (+ 1 (shift k (k (k 5))))))
```

In diesem Fall wird `k` zwei Mal auf `5` angewendet, die Berechnung entspricht also 
`(* 2 (+ 1 (+ 1 5)))` und das Ergebnis ist `14`.

## Interpreter mit Delimited Continuations
Wir können im CPS-transformierten Interpreter mit wenig Aufwand Sprachkonstrukte hinzufügen, die `shift` und `reset` aus Racket entsprechen. Wir ergänzen `Exp` um die zwei neuen Cases `Shift` und `Reset`:
```scala
case class Shift(param: String, body: Exp) extends Exp
case class Reset(body: Exp) extends Exp
```

Wir müssen den Typ des Interpreters ändern, da Aufrufe von Delimited Continuations zurückkehren und die Berechnung nicht abbrechen. Dadurch haben Continuations den Typ `Value => Value` und der Interpreter gibt somit auch einen `Value` zurück. Im Interpreter ergänzen wir die entsprechenden Fälle und passen den `App`-Fall an (Änderungen mit `<--` markiert):
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

Im `Reset`-Fall wird die aktuelle Continuation zurückgesetzt, indem die Identitätsfunktion anstelle von `k` weitergereicht wird. Im `Shift`-Fall wird wie im `LetCC`-Fall die aktuelle Continuation "umhüllt" und in der Umgebung an den Identifier gebunden. Außerdem wird hier auch die Continuation zurückgesetzt (weil dies dem Verhalten in Racket entspricht). Bei der Applikation von Continuations wird nun die aktuelle Continuation nicht mehr verworfen, sondern wird mit dem Ergebnis der Applikation aufgerufen, d.h. es findet eine Komposition der aktuellen Continuation nach der aufgerufenen Continuation statt.


# Monaden
In unseren bisherigen Interpretern haben wir bereits einige verschiedene "Rekursions-Patterns" gesehen: 
- Im [ersten Interpreter](https://pad.its-amazing.de/programmiersprachen1teil1#Erster-Interpreter-AE) haben wir direkte Rekursion, bei der die `eval`-Funktion rekursiv auf den Unterausdrücken aufgerufen wird. Die rekursive Auswertung entspricht exakt der rekursiven Datenstruktur, in denen die Programme repräsentiert sind.

- Bei der Einführung von Environments in [AEId](https://pad.its-amazing.de/programmiersprachen1teil1#Identifier-mit-Umgebung-AEId) oder in [FAE](https://pad.its-amazing.de/programmiersprachen1teil1#Closures) wird bspw. die Environment bei der rekursiven Auswertung im abstrakten Syntaxbaum (AST) nach unten weitergereicht, d.h. der zusätzliche Parameter `env` wird entlang der Datenstruktur propagiert. 

- Bei der Ergänzung von mutierbaren Boxen in [BCFAE](https://pad.its-amazing.de/programmiersprachen1teil1#Interpreter) haben wir den `Store`-Parameter hinzugefügt, der im AST auf einer Ebene erst bei einem rekursiven Aufruf übergeben wird, dann (evtl. modifiziert) zurückkehrt und beim nächsten rekursiven Aufruf wieder übergeben wird.

- Der CPS-transformierte Interpreter entspricht dem Continuation Passing Style, den wir bereits ausführlich besprochen haben. 

:::success
Monad-Intro mit Option-Monad
:::

Monaden können aufgefasst werden als Möglichkeit, solche Rekursionstile bzw. -patterns zu definieren und darüber zu abstrahieren.
```scala
trait Monad[M[_]] {
    def unit[A](a: A) : M[A]
    def bind[A,B](m: M[A], f: A => M[B]) : M[B]
  }
```

Ein Monad ist ein Tripel aus einem Typkonstruktor (`M[_]`) und zwei Funktionen, nämlich `unit` und `bind`. Dabei müssen die folgenden _Monad-Gesetze_ gelten:
- `bind(unit(x),f) == f(x)`
- `bind(x, y => unit(y)) == x`
- `bind(bind(x,f),g) == bind(x, y => bind(f(y),g))`

D.h. `unit` ist eine Art "neutrales Element" und `bind` ist assoziativ. 

In Scala kann mit der folgenden Funktion die Syntax der `for`-Comprehensions für das Programmieren mit Monaden genutzt werden:
```scala
implicit def monadicSyntax[A, M[_]](m: M[A])(implicit mm: Monad[M]) = new {
    def map[B](f: A => B): Any = mm.bind(m, (x: A) => mm.unit(f(x)))
    def flatMap[B](f: A => M[B]): M[B] = mm.bind(m, f)
  }
```

Diese Syntax kann für alle Datentypen angewendet werden, für die `map` und `flatMap` definiert ist. Durch die obige implizite Definition, in der wir `map` und `flatMap` so definieren, dass sie gerade der `unit`- und der `bind`-Operation entsprechen, sorgen wir dafür, dass die `for`-Comprehension-Syntax für die `Monad`-Klasse genutzt werden kann.

Monaden dienen zur Funktionskomposition für Fälle, in denen der Rückgabetyp einer Funktion nicht dem Parametertyp der danach anzuwendenden Funktion entspricht, sondern ein "Zwischenschritt" notwendig ist.











:::success
- [ ] VL 16
- [ ] Mark & Sweep fertig zusammenfassen (???)
- [ ] Fixpunkt-Kombinator
- [ ] PPI
- [ ] Alle Vorlesungen bis 14. 10. - 2 Wochen für Lecture Notes, Wiederholungen, Lernen, Probeklausur, evtl. Altklausuren
:::
