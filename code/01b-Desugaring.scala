sealed trait CExp
case class CNum(n: Int) extends CExp
case class CAdd(l: CExp, r: CExp) extends CExp
case class CMul(l: CExp, r: CExp) extends CExp

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