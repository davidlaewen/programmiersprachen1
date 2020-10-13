import scala.language.higherKinds
import scala.language.implicitConversions

trait Monad[M[_]] {
  def unit[A](a: A): M[A]
  def bind[A,B](m: M[A], f: A => M[B]): M[B]
  // The "monad laws":
  // 1) "unit" acts as a kind of neutral element of "bind", that is:
  //    1a) bind(unit(x),f) == f(x) and
  //    1b) bind(x, y => unit(y)) == x
  // 2) Bind enjoys an associative property
  //     bind(bind(x,f),g) == bind(x, y => bind(f(y),g))
}

implicit def monadicSyntax[A, M[_]](m: M[A])(implicit mm: Monad[M]) = new {
  def map[B](f: A => B) : M[B] = mm.bind(m, (x: A) => mm.unit(f(x)))
  def flatMap[B](f: A => M[B]) : M[B] = mm.bind(m, f)
}

object OptionMonad extends Monad[Option] {
  override def bind[A,B](a: Option[A], f: A => Option[B]) : Option[B] =
    a match {
      case Some(x) => f(x)
      case None => None
    }
  override def unit[A](a: A) = Some(a)
}


/** Program to rewrite: */
case class Person(id : Int, firstName : String, lastName : String)
case class Department(id : Int, head : Person, members : Map[Int, Person])
case class Company(departments : Map[Int, Department])

val testPerson1 = new Person(21, "Heiner", "Hacker")
val testPerson2 = new Person(42, "Heinrich", "Knacker")
val testDepartment = new Department(1, testPerson1, Map(21 -> testPerson1, 42 -> testPerson2))
val testCompany = new Company(Map(1 -> testDepartment))

def findEmployeeLastName(company: Company, depId: Int, personId: Int) : Option[String] =
  company.departments.get(depId) match {
    case Some(dep) => dep.members.get(personId) match {
      case Some(person) => Some (person.lastName)
      case None => None
    }
    case None => None
  }

def findEmployeeSuperior(company: Company, depId: Int, personId: Int) : Option[Person] =
  company.departments.get(depId) match {
    case Some(dep) => dep.members.get(personId) match {
      case Some(person) => Some (dep.head)
      case None => None
    }
    case None => None
  }

def test() : Unit = {
  println(findEmployeeLastName(testCompany,1,21))
  println(findEmployeeSuperior(testCompany,1,42))
}

/** Rewritten using for-comprehension */
object Rewritten {

  def findEmployeeLastName(company: Company, depId: Int, personId: Int) : Option[String] =
    for {
      dep <- company.departments.get(depId)
      person <- dep.members.get(personId)
    } yield person.lastName

  def findEmployeeSuperior(company: Company, depId: Int, personId: Int) : Option[Person] =
    for {
      dep <- company.departments.get(depId)
      person <- dep.members.get(personId)
    } yield dep.head

  def test() : Unit = {
    println(findEmployeeLastName(testCompany,1,21))
    println(findEmployeeSuperior(testCompany,1,42))
  }

  def test2() : Unit = println(findEmployeeSuperior(testCompany,1,41))

}


/** Incorrect solution */
object Incorrect {

  def findEmployeeSuperior(company : Company, depId : Int, personId : Int): Option[Person] =
    for {
      dep <- company.departments.get(depId)
    } yield dep.head

  def test2() : Unit = println(findEmployeeSuperior(testCompany,1,41))

}

def test2() : Unit = println(findEmployeeSuperior(testCompany,1,41))

/* 'test2' and 'Incorrect.test2' show the difference between the original function and the incorrectly rewritten
function. The original implementation of 'findEmployeeSuperior' only returns the department head if 'personId' can be
found in the department, else 'None' is returned. The incorrect solution always returns the department head without
checking if the person exists and is a member of the department.
The incorrect solution is not a refactoring since it changes the semantics.
 */

