import scala.language.implicitConversions

sealed abstract class Exp 
case class Num(n: Int) extends Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Id(x: String) extends Exp

implicit def int2exp(n: Int) = Num(n)
implicit def string2exp(s: String) = Id(s)

case class FunDef(params: List[String], body: Exp)
case class Call(f: Either[String,FunDef], args: List[Exp]) extends Exp

case class With(x: String, xDef: Either[Exp,FunDef], body: Exp) extends Exp


def subst(e: Exp, i: String, v: Either[Num,FunDef]) : Exp =  e match {
    case Num(n) => e
    case Id(x) => if (x == i) v match {
      case Left(n) => n
      case Right(_) => sys.error("Funs can only be applied")
    } else e
    case Add(l,r) => Add( subst(l,i,v), subst(r,i,v))
    case With(x,xDef,body) => xDef match {
      case Left(e1) => With(x, Left(subst(e1,i,v)), if (x == i) body else subst(body,i,v))
      case Right(FunDef(params,body)) => With(x, xDef, if (x == i) body else subst(body,i,v))
    }
    case Call(f,args) => if (f == Left(i)) v match {
      case Left(_) => sys.error("Can only call funs")
      case Right(fDef) => Call(Right(fDef),args.map(subst(_,i,v)))
    } else Call(f,args.map(subst(_,i,v)))
}

def eval(e: Exp) : Int = e match {
  case Num(n) => n
  case Id(x) => sys.error("Unbound identifier: " + x)
  case Add(l,r) => eval(l) + eval(r)
  case With(x, xDef, body) => xDef match {
    case Left(e1) => eval(subst(body,x,Left(Num(eval(e1)))))
    case Right(fd) => eval(subst(body,x,Right(fd)))
  }
  case Call(fDef,args) => fDef match {
    case Left(fId) => sys.error("Unbound function: "+fId)
    case Right(FunDef(params,body)) =>
      val vArgs = args.map( eval(_) )
      if (params.size != vArgs.size) 
        sys.error("Incorrect number of args")
      val substBody = params.zip(vArgs).foldLeft(body)(
        (b,av) => subst(b,av._1,Left(Num(av._2)))
      )
      eval(substBody)
  }
}

val ex1 = With("f", Right(FunDef(List("x"), Add("x",1))), With("a", Left(2), Call(Left("f"),List("a"))))
val ex2 = With("f", Right(FunDef(List("x"), Add("x",1))), With("a", Left(2), Call(Left("f"),List("f"))))
