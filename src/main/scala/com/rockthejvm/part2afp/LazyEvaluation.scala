package com.rockthejvm.part2afp

object LazyEvaluation {

  lazy val x: Int = { // lazy will delay the evaluation of x until first use (evaluation)
    println("Hello")
    42
  } // EVALUATION OCCURS ONCE

  /*
  Example 1: call by need
   */

  def byNameMethod(n: => Int): Int =
    n + n + n + 1

  def retrieveMagicValue() = {
    println("waiting...")
    Thread.sleep(1000)
    42
  }

  def demoByName(): Unit = {
    println(byNameMethod(retrieveMagicValue()))
    // retrieveMagicValue() + retrieveMagicValue() + retrieveMagicValue() + 1
    // we'll see println("waiting...") three times, each one a second apart
    // then we'll see whatever 42*3+1 is printed
  }

  // call by need = call by name + lazy values
  def byNeedMethod(n: => Int): Int = {
    lazy val lazyN = n // memoization
    lazyN + lazyN + lazyN + 1
  }

  def demoByNeed(): Unit = {
    println(byNeedMethod(retrieveMagicValue()))
  }

  /*
  Example 2: withFilter
   */

  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1,25,40,5,23)

  def demoFilter(): Unit = {
    val lt30 = numbers.filter(lessThan30)
    val gt20 = lt30.filter(greaterThan20)
    println(gt20)
  }
  def demoWithFilter(): Unit = {
    val lt30 = numbers.withFilter(lessThan30)
    val gt20 = lt30.withFilter(greaterThan20)
    println(gt20.map(x => x))
  }

  def demoForComprehension(): Unit = {
    val forComp = for {
      n <- numbers if lessThan30(n) && greaterThan20(n)
    } yield n
    println(forComp)
  }


  def main(args: Array[String]): Unit = {
    println(x)
    println(x) // second call doesn't print Hello because x is already evaluated
    // demoByName()
    // demoByNeed()
//    println("Filter:")
//    demoFilter()
//    println("withFilter:")
//    demoWithFilter()
    demoForComprehension()
  }
}
