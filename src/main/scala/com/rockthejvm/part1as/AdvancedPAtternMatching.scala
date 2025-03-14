package com.rockthejvm.part1as

object AdvancedPAtternMatching {

  /*
  PM:
  - constants
  - objects
  - wildcards
  - variables
  - infix patterns
  - lists
  - case classes
   */

  class Person(val name: String, val age: Int)

  object Person {
    def apply: Any = ???
    def unapply(person: Person): Option[(String, Int)] =
      if (person.age < 21) None
      else Some((person.name, person.age))

    def unapply(age: Int): Option[String] =
      if (age < 21) Some("minor")
      else Some("Legally allowed to drink")
  }

  val daniel = new Person("Daniel", 102)
  val danielPM = daniel match { // Person.unapply(daniel) => Option((n,a))
    case Person(n, a) => s"Hi there, I'm $n"
  }

  val danielLegalStatus = daniel.age match {
    case Person(status) => s"Daniel's legal driking status is $status"
  }

  // boolean matching

  /*
  aNumber match {
  case prop1 => ..
  case prop2 => ..
  case prop3 => ..
  }
   */
  object even {
    def unapply(arg: Int): Boolean = arg % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg > -10 && arg < 10
  }

  val n: Int = 43

  val mathProperty = n match {
    case even() => "an even number"
    case singleDigit() => "a one digit number"
    case _ => "no special property"
  }

  // infix patterns
  infix case class Or[A, B](a: A, b: B)
  val anEither = Or(2, "Two")
  val humanDescriptionEither = anEither match {
    case number Or string => s"$number is written as $string" // we can do this because case classes have companion object implementing unapply method
  }

  val aList = List(1,2,3)
  val ListPM = aList match {
    case 1 :: rest => "a list starting with 1" // is because there is a pattern :: (object ::) with two arguments, 1 and another list
    case _ => "some uninteresting list"
  }

  // decomposing sequences
  val vararg = aList match {
    case List(1, _*) => "List starting with 1"
    case _ => "Some other list"
  }

  abstract class MyList[A] {
    def head: A = throw new NoSuchElementException
    def tail: MyList[A] = throw new NoSuchElementException
  }

  case class Empty[A]() extends MyList[A]
  case class Cons[A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] = {
      if (list == Empty()) Some(Seq.empty)
      else unapplySeq(list.tail).map(restOfSequence => list.head +: restOfSequence)
    }
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty())))
  val varargCustom = myList match {
    case MyList(1, _*) => "list starting with 1"
    case _ => "some other list"
  }

  // we don't necessarily have to return an option, we have to return any type that has two methods:
  // isEmpty() and get()
  // custom return type
  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = person.name
    }
  }

  val weirdPersonPM = daniel match {
    case PersonWrapper(name) => s"Hey, my name is $name"
  }

  def main(args: Array[String]): Unit = {
    println(danielPM)
    println(danielLegalStatus)
    println(mathProperty)
  }
}
