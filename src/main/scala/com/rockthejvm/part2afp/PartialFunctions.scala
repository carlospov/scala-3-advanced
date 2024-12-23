package com.rockthejvm.part2afp

object PartialFunctions {

  val aFunction: Int => Int = x => x + 1

  val aFuzzyFunction = (x: Int) => {
    if (x==1) 42
    else if (x==2) 56
    else if (x==5) 999
    else throw new RuntimeException("no suitable cases possible")
  }

  val aFuzzyFunction_v2 = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
    case _ => throw new RuntimeException("no suitable cases possible")
  }

  // these are partial functions, they're only applicable to a certain inputs
  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }

  val canCallOn37 = aPartialFunction.isDefinedAt(37)

  // we can transform a partial function into a full function that returns None if not defined
  val liftedPF = aPartialFunction.lift

  // other methods
  val anotherPF: PartialFunction[Int, Int] = {
    case 45 => 86
  }

  val pfChain = aPartialFunction.orElse[Int, Int](anotherPF) // runs first function on the argument, if not supported, then runs second

  // HOFs accepts pf as arguments
  val aList = List(1,2,3,4)
  val aChangedList = aList.map(x => x match {
    case 1 => 4
    case 2 => 3
    case 3 => 45
    case 4 => 67
    case _ => 0
  })
    val aChangedList_v2 = aList.map({ // possible because PartialFunction[A, B] extends Function1[A, B]
    case 1 => 4
    case 2 => 3
    case 3 => 45
    case 4 => 67
    case _ => 0
  })
    val aChangedList_v3 = aList.map { // without parenthesis
    case 1 => 4
    case 2 => 3
    case 3 => 45
    case 4 => 67
    case _ => 0
  }

  case class Person(name: String, age: Int)
  val somePeople = List(
    Person("Alice", 3),
    Person("Bob", 5),
    Person("Jane", 8)
  )

  val kidsGrowingUp = somePeople.map {
    case Person(name, age) => Person(name, age + 1)
  }



  def main(args: Array[String]): Unit = {
    println(aPartialFunction(2))
//    println(aPartialFunction(245)) // match error, not defined on that value, it is based on pattern matching
    // we can test if a partial function is applicable without actually evaluating
    println(canCallOn37)
    println(liftedPF(5))
    println(liftedPF(37)) // returns None, not an error like partial functions do
    println(pfChain(45)) // returns second pf image of 45
  }
}
