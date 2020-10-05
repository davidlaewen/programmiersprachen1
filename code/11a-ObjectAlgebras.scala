import scala.language.implicitConversions

trait Bool {
  def ifThenElse[T](t: T, e: T) : T
}

case object T extends Bool {
  def ifThenElse[T](t: T, e: T) : T = t
}

case object F extends Bool {
  def ifThenElse[T](t: T, e: T) : T = e
}

def not(b: Bool) : Bool = b.ifThenElse(F,T)
def and(a: Bool, b: Bool) : Bool = a.ifThenElse(b,F)
def or(a: Bool, b: Bool) : Bool = a.ifThenElse(T,b)


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

case class Succ(n: Num) extends Num {
  def fold[T](s: T => T, z: T) : T = s(n.fold(s,z))
}

def printNum(n: Num) : Unit = n.fold[Unit](_ => print("."),())

case class Add(a: Num, b: Num) extends Num {
  def fold[T](s: T => T, z: T) : T = a.fold(s, b.fold(s,z))
}




