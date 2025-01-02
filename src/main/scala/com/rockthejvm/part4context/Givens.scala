package com.rockthejvm.part4context

object Givens {

  // list sorting
  val aList = List(4,5,1,2,9,0,3)
  val anOrderedList = aList.sorted

  // explicitly passing ordering as argument
  val descendingOrdering_v0: Ordering[Int] = Ordering.fromLessThan(_ > _)
  val anInverseOrderedList = aList.sorted(descendingOrdering_v0)

  // stating 'given' we pass argument implicitly, now the compiler knows this is the default ordering for Int
  given descendingOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  // whenever we state a given instance in the scope of a magical method, that magical method will take it as its argument

  // custom sorting
  case class Person(name: String, age: Int)
  val people = List(Person("Alice", 29), Person("Sarah", 21), Person("Nil", 9), Person("Bob", 31))

  given personOrdering: Ordering[Person] = new Ordering[Person] {
    override def compare(x: Person, y: Person): Int =
      x.name.compareTo(y.name) // -someInt if x < y (alphabetical order bc we are comparing strings), +someInt if y < x and 0 if equal
  }

  // val sortedPerople = people.sorted(personOrdering) // without "given"
  val sortedPerople = people.sorted // with "given" it's automatically passed by the compiler

  object PersonAllSyntax { // other alternative syntax for given, using "with"
    given personOrdering: Ordering[Person] with { // inside the same scope we cannot create two given instances for the same ordering
      override def compare(x: Person, y: Person): Int =
      x.name.compareTo(y.name)
    }
  }

  // using clauses
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }

  //def combineAll[A](list: List[A])(combinator: Combinator[A]): A =
  //  list.reduce(combinator.combine)
  /* I want to say
  combineAll(List(1,2,3,4))
  combineAll(people)
   */
  // that's achieved with "using"
  def combineAll[A](list: List[A])(using combinator: Combinator[A]): A =
    list.reduce(combinator.combine)

  given IntCombinator: Combinator[Int] with {
    override def combine(x: Int, y: Int): Int = x + y
  }

  val firstSum = combineAll(List(1,2,3,4)) // IntCombinator passed automatically by the compiler
  //val combineAllPeople = combineAll(people) // wont compile unless combinator of person in scope

  // context bound
  def combineInGroups3[A](list: List[A])(using combinator: Combinator[A]): List[A] = {
    list.grouped(3).map(group => combineAll(group)/*(combinator) passed by the compiler as the given Combinator[A]*/).toList
  }

  def combineInGroups3_v2[A : Combinator](list: List[A]): List[A] = { // A : Combinator states that there is a given Combinator of type A in scope and is called a "context bound"
     list.grouped(3).map(group => combineAll(group)).toList
  }

  // synthesize new given instances based on existing ones
  given listOrdering(using intOrdering: Ordering[Int]): Ordering[List[Int]] with {
    override def compare(x: List[Int], y: List[Int]) =
      x.sum - y.sum // + if xsum > ysum, - if ysum > xsum, else 0
  }

  val listOfLists = List(List(1,2), List(3,4), List(5))
  val nestedListsOrdered = listOfLists.sorted

  // same but with generics
  given listOrderingBasedCombinator[A](using ord: Ordering[A])(using combinator: Combinator[A]): Ordering[List[A]] with {
    override def compare(x: List[A], y: List[A]): Int =
      ord.compare(combineAll(x), combineAll(y))
  }

  // pass a regular value in place of a given
  val myCombinator = new Combinator[Int] {
    override def combine(x: Int, y: Int): Int = x * y
  }

  val listProduct = combineAll(List(1,2,3,4))(using myCombinator) // using has a different meaning, it overrides the default given instance with the one specified

  def main(args: Array[String]): Unit = {
    println(anOrderedList)
    println(anInverseOrderedList)
    println(sortedPerople)
  }
}
