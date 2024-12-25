package com.rockthejvm.part2afp

import scala.annotation.targetName

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

  // exercise: IS THIS A MONAD OR NOT?
  // answer: IT IS
  // interpretation: this class wraps ANY computation that might produce side effects
  // this "PossiblyMonad" class separates the description of a computation from the actual performance of that computation
  // the main advantage is that we can combine maps, flatMaps, for, etc. on these possible computations without actually
  // performing any computation
  case class IO[A](unsafeRun: () => A) {

    def map[B](f: A => B): IO[B] =
      IO(() => f(unsafeRun()))

    def flatMap[B](f: A => IO[B]): IO[B] =
      IO(() => f(unsafeRun()).unsafeRun())
  }

  object IO {
    @targetName("pure")
    def apply[A](value: => A): IO[A] =
      new IO(() => value)
  }

  def possiblyMonadStory(): Unit = {
    val aPossiblyMonad = IO(4)
    val f = (x: Int) => IO(x + 1)
    val g = (x: Int) => IO(2 * x)
    val pure = (x: Int) => IO(x)

    val LeftIdentity = pure(4).flatMap(f) == f(4)
    val RightIdentity = aPossiblyMonad.flatMap(pure) == aPossiblyMonad
    val associativity = aPossiblyMonad.flatMap(f).flatMap(g) == aPossiblyMonad.flatMap(x => f(x).flatMap(g))

    println(LeftIdentity)
    println(RightIdentity)
    println(associativity)

    //
    println(IO(3) == IO(3)) // false because it constructs a new lambda each time, is not possible to determine what will arise from
    // that lambdas, they just aren't the same thing, even when they return the same value. Although the three properties are false, they're false negatives

    val LeftIdentity_v2 = pure(4).flatMap(f).unsafeRun() == f(4).unsafeRun()
    val RightIdentity_v2= aPossiblyMonad.flatMap(pure).unsafeRun() == aPossiblyMonad.unsafeRun()
    val associativity_v2 = aPossiblyMonad.flatMap(f).flatMap(g).unsafeRun() == aPossiblyMonad.flatMap(x => f(x).flatMap(g)).unsafeRun()

    println(LeftIdentity_v2) // true on values produced...
    println(RightIdentity_v2) // true on values produced...
    println(associativity_v2) // true on values produced...

    val fs = (x: Int) => IO {
      println("Incrementing")
      x + 1
    }
    val gs = (x: Int) => IO {
      println("doubling")
      x * 2
    }

    val associativity_v3 = aPossiblyMonad.flatMap(fs).flatMap(gs).unsafeRun() == aPossiblyMonad.flatMap(x => fs(x).flatMap(gs)).unsafeRun()

  }

  def PossiblyMonadExample(): Unit = {
    val aPossiblyMonad = IO {
      println("Printing inside monad")
      // computations
      42
    }

    val anotherPossiblyMonad = IO {
      println("Printing inside another monad")
      // computations
      "Scala"
    }

//    val aResult = aPossiblyMonad.unsafeRun()
//    println(aResult)

    val aForComprehension = for { // doing this won't execute what's inside the monads until value is evaluated inside an expression
      num <- aPossiblyMonad       // this allows us to perform functional programming without executing anything until the last moment
      lang <- anotherPossiblyMonad // this PossiblyMonad Type is called "IO" in CatsEffect and "ZIO" in ZIO
    } yield s"$num-$lang"
  }

  def main(args: Array[String]): Unit = {
    possiblyMonadStory() // prints side effects of fs and gs in same order
    // PossiblyMonadExample()
  }
}
