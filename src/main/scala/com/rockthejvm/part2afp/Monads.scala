package com.rockthejvm.part2afp

object Monads {

  def listStory(): Unit = {
    val aList = List(1,2,3)
    val listMultiply = for {
      x <- List(1,2,3)
      y <- List(4,5,6)
    } yield x * y
    // for-comprehensions == chains of map + flatMap
    val listMultipy_v2 = List(1,2,3).flatMap(x => List(4,5,6).map(y => x * y))

    val f = (x: Int) => List(x, x + 1)
    val g = (x: Int) => List(x, 2 * x)
    val pure = (x: Int) => List(x) // same as list "constructor"

    // prop 1: Left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for every x, for every function

    // prop 2: Right-Identity
    val rightIdentity = aList.flatMap(pure) == aList // for every list

    // prop 3: Associativity
    val associativity = aList.flatMap(f).flatMap(g) == aList.flatMap(x => f(x).flatMap(g)) // can be mathematically proved
    // associativity holds for lists, but this property depends directly on the implementation of the flatMap method
  }

  def optionStory(): Unit = {
    val anOption = Option(42)
    val optionString = for {
      lang <- Option("Scala")
      version <- Option(3)
    } yield s"$lang-$version"

    val optionString_v2 = Option("Scala").flatMap(lang => Option(3).map(version => s"$lang-$version"))

    val f = (x: Int) => Option(x + 1)
    val g = (x: Int) => Option(x * 2)
    val pure = (x: Int) => Option(x) // same as option constructor

    // prop 1 : Left-identity
    val leftIdentity = pure(42).flatMap(f) == f(42) // for any x, for any f

    // prop 2: Right-identity
    val rightIdentity = anOption.flatMap(pure) == anOption // for any option

    // prop 3: associativity
    val associativity = anOption.flatMap(f).flatMap(g) == anOption.flatMap(x => f(x).flatMap(g)) // true for any option, any f and any g
  }


  // MONADS => data structures that wrap other data structures and make them suitable for chain dependant computation
  // through the flatMap operation attached to it. flatMap must satisfy properties 1, 2 and 3 for a ds to be a monad



  def main(args: Array[String]): Unit = {

  }
}
