package com.rockthejvm.part1as

import scala.annotation.targetName
import scala.util.Try

object DarkSugars {

  // 1 - sugar for methods with ONE ARGUMENT
  def singleArgMethod(arg: Int): Int = arg + 1

  val aMethodCall = singleArgMethod({
    // long code
    42
  })

  val aMethodCall_v2 = singleArgMethod {
    // long code
    42
  }

  // example: Try, Future
  val aTryInstance = Try({
    throw new RuntimeException
  })

  // example: Try, Future
  val aTryInstance_v2 = Try {
    throw new RuntimeException
  }

  // with hofs
  val anIncrementedList = List(1,2,3).map { x =>
    // code block
    x + 1
  }

  // 2 - single abstract method pattern (since scala 2.12)
  trait Action {
    // can also have another IMPLEMENTED methods/fields here
    def act(x: Int): Int
  }

  val anAction = new Action {
    override def act(x: Int): Int = x + 1
  }

  val anotherAction: Action = (x: Int) => x + 1 // the compiler decomposes this as new Action { override def act(x: Int): Int = x + 1 }

  anotherAction.act(2)

  // example: Runnable
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hi Scala, from another thread")
  })

  val aSweeterThread= new Thread(() => println("Hi, Scala"))

  // 3 - Methods ending in a : are RIGHT-ASSOCIATIVE
  val aList = List(1,2,3)
  val aPrependedList = 0 :: aList // is not 0.::(aList), the :: is on the List type and the compiler rewrites it into smth as the following

  val aThing = aList.::(0)
  val aBigList = 0 :: 1 :: 2 :: List(3,4) // the right-most operand is evaluated first, and so on
  // rewritten to List(3,4).::(2).::(1).::(0)

  // we can define our own :-ending methods
  class MyStream[T] {
    infix def -->:(value: T): MyStream[T] = this
  }

  val myStream = 1 -->: 2 -->: 3 -->: 4 -->: new MyStream[Int]

  // 4 - multi-word identifiers
  class Talker(name: String) {
    infix def `and then said`(gossip: String) = println(s"$name said $gossip")
  }

  val daniel = new Talker("Daniel")
  val danielStatement = daniel `and then said` "I love Scala"

  // example: HTTP libraries
  object `Content-Type` {
    val `aplication/json` = "application/JSON"
  }

  // 5 - infix types
  @targetName("Arrow") // to make it more readable + Java interop
  infix class -->[A, B]
  val compositeType: Int --> String = new -->[Int, String] // -->[Int, String] = new -->[Int, String]

  // 6 - update
  val anArray = Array(1,2,3,4)
  val anArrayUpdated = anArray.update(2, 45)
  val anArrayUpdated_v2 = anArray(2) = 45 // same, and can be used in every other structure with an update method taking 2 arguments

  // 7 - mutable fields
  class Mutable {
    private var internalMember: Int = 0
    def member = internalMember // "getter"
    def member_=(value: Int): Unit =
      internalMember = value
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // this is rewritten as aMutableContainer.member_=(42)

  // 8 - variable arguments (var args)
  def methodWithVargs(args: Int*) = {
    // return the number of arguments supplied
    args.length // args has the SAME API as a Seq
  }

  val callWithZeroArgs = methodWithVargs()
  val callWithOneArgs = methodWithVargs(2)
  val callWithTwoArgs = methodWithVargs(2, 4)

  val aCollection = List(1,2,3,4)
  val callWithDynamicArgs = methodWithVargs(aCollection*) // this will unwrap contents of the list and pass it as arguments of the method

  def main(args: Array[String]): Unit = {
    println(aMethodCall_v2)
  }
}
