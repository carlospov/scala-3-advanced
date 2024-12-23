package com.rockthejvm.part2afp

object CurryingPAFs {

  // currying: passing arguments one at a time
  val superAdder: Int => Int => Int = x => y => x + y
  val add3 = superAdder(3) // Int => Int = y => 3 + y
  val eight = add3(5) // 8
  val eight_v2 = superAdder(3)(5)

  // also available as methods: curried methods
  def curriedAdder(x: Int)(y: Int): Int =
    x + y

  // methods != function values
  // but can be converted
  val add4 = curriedAdder(4) // eta-expansion: the process that follows the compiler to convert method to function value
  val nine = add4(5)

  def increment(x: Int): Int = x + 1
  val aList = List(1,2,3)
  val anIncrementedList = aList.map(increment) // eta-expansion

  // underscores as a way to convert methods to functions
  def concatenator(a: String, b: String, c: String): String  = a + b + c
  val insertName = concatenator("Hello, my name is ", _: String, " , I'm going to show you a Scala trick.") // x => concatenator("...", x, "...")

  val danielsGreeting = insertName("Daniel")

  val fillInTheBlanks = concatenator(_: String, "Daniel", _: String) // x, y => concatenator(x,"...", y)
  val danielsGreeting_v2 = fillInTheBlanks("Hi", "how are you?")

  /**
   * Exercises
   * 1. Obtain an add7 function out of these 3 definitions of adders, create as many as you can
   * 2. Process a list of numbers and return their string representations under different formats
   * @param args
   */

  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def CurriedMethod(x: Int)(y: Int) = x + y

  // 1 - add7
  val add7 = (x: Int) => simpleAddFunction(x, 7)
  val add7_v2 = (x: Int) => simpleAddFunction(7, x)
  val add7_v3 = simpleAddMethod(7, _)
  val add7_v4 = simpleAddMethod(_, 7)
  val add7_v5 = CurriedMethod(7)(_)
  val add7_v6 = CurriedMethod(7)
  val add7_v7 = x => CurriedMethod(7)(x)
  val add7_v8 = (x: Int) => CurriedMethod(x)(7)
  val add7_v9 = simpleAddFunction.curried(7)

  // 2 -
  // 2.1 create a curried formatting method to make the kind of values that follow
  val piWith2Dec = "%4.2f".format(Math.PI)
  val piWith2Dec_v2 = "%8.6f".format(Math.PI)
  // 2.2 process a list of numbers with various formats

  def curriedFormatting(format: String)(n: Double) =
    val fstring = s"%${format}f"
    fstring.format(n)

  // methods vs functions + by-name vs 0-lambdas
  def byName(n: => Int) = n + 1
  def byLambda(f: () => Int) = f() + 1

  def method: Int = 42
  def parenthmethod(): Int = 42

  byName(23) // ok
  byName(method) // 43, eta-expanded? NO, method is simply INVOKED here
  byName(parenthmethod()) // 43, simple
  // byName(parenthmethod) // I cannot do that, code won't compile (since Scala3)
  byName((() => 42)()) // 43, ok
  // byName(() => 42) // NOT ok

  // byLambda(23) // not ok
  // byLambda(method) // not ok, won't compile, the method won't be eta-expanded
  byLambda(parenthmethod) // Ok, eta-expansion is done
  byLambda(() => 42)
  byLambda(() => parenthmethod()) // ok, this is exactly how compiler rewrites byLambda(parenthmethod) when doing eta-expansion



  def main(args: Array[String]): Unit = {
    println(add7(2))
    println(add7_v2(2))
    println(add7_v3(2))
    println(add7_v4(2))
    println(add7_v5(2))
    println(add7_v6(2))

    val aList = List(1.2345,2.3456,3.4567,4.5678)
    println(aList.map(curriedFormatting("4.2")))
    println(aList.map(curriedFormatting("4.7")))
    println(aList.map(curriedFormatting("3.1")))
  }
}
