/** Church Encoding in Scala */

trait Bool {
  def ifThenElse[T](t: T, e: T) : T
}

case object True extends Bool {
  def ifThenElse[T](t: T, e: T) : T = t
}

case object False extends Bool {
  def ifThenElse[T](t: T, e: T) : T = e
}

def not(b: Bool) : Bool = b.ifThenElse(False,True)
def and(a: Bool, b: Bool) : Bool = a.ifThenElse(b,False)
def or(a: Bool, b: Bool) : Bool = a.ifThenElse(True,b)

assert(not(False) == True)
assert(and(True,False) == False)
assert(or(False,True) == True)


trait Num {
  def fold[T](s: T => T, z: T) : T
}

case object Zero extends Num {
  def fold[T](s: T => T, z: T) : T = z
}

case object One extends Num {
  def fold[T](s: T => T, z: T) : T = s(z)
}

case object Two extends Num {
  def fold[T](s: T => T, z: T): T = s(s(z))
}

def printNum(n: Num) : String = n.fold[String](_+".","")
def convertNum(n: Num) : Int = n.fold[Int](_+1, 0)

case class Succ(n: Num) extends Num {
  def fold[T](s: T => T, z: T) : T = s(n.fold(s,z))
  // def fold[T](s: T => T, z: T) : T = n.fold(s, s(z))
}

def succ(n: Num) : Num = {
  new Num {
    def fold[T](s: T => T, z: T) : T = s(n.fold(s,z))
  }
}

val three = Succ(Two)
val four = Succ(three)

case class Add(a: Num, b: Num) extends Num {
  def fold[T](s: T => T, z: T) : T = a.fold(s, b.fold(s,z))
}

case class Mul(a: Num, b: Num) extends Num {
  def fold[T](s: T => T, z: T) : T = a.fold((z1: T) => b.fold(s,z1), z)
}

def mul(a: Num, b: Num) : Num = {
  new Num {
    def fold[T](s: T => T, z: T) : T = a.fold((z1: T) => b.fold(s,z1), z)
  }
}

def exp(a: Num, b: Num) : Num = b.fold(z => Mul(a,z), One)

assert(convertNum(Add(One,Two)) == 3)
assert(convertNum(Mul(three,four)) == 12)



