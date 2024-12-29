package com.rockthejvm.part3async

import scala.collection.parallel.ParSeq
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.CollectionConverters.*

object ParallelCollections {

  val aList = (1 to 1000000).toList
  val anIncrementedList = aList.map(_ + 1)
  val parList: ParSeq[Int] = aList.par
  val aParallelizedIncementedList = parList.map(_ + 1) // map, flatmap, filter, foreach, reduce, fold
  /*
  Applicable for
  - sequences
  - vectors
  - arrays
  - maps
  - sets

  use cases: faster processing
   */

  // we can build a parallel collection explicitly
  val aParVector = ParVector[Int](1,2,3,4,5,6,7,8,9)

  def measure[T](expression: => T): Long = {
    val time = System.currentTimeMillis()
    expression // forcing evaluation
    System.currentTimeMillis() - time
  }

  def compareListTransformation(): Unit = {
    val list = (1 to 30000000).toList
    println("List creation completed")

    val serialTime = measure(list.map(_ + 1))
    println(s"[serial time] $serialTime")
    val parallelTime = measure(list.par.map(_ + 1))
    println(s"[parallel time] $parallelTime")
    }

  // caveats of using par vs serial
  // some operations can be indeterministic
  def demoUndefinedOrder(): Unit = {
    val aList = (1 to 1000).toList
    val reduction = aList.reduce(_ - _) // from an element we subtract the following one, non-associative operation CAUTION!
    // this is where the pitfall lies
    // we might be tempted to use the parallel version of a list, but this will cause problems due to the reduce implementation

    val parallelReduction = aList.par.reduce(_ - _)
    println(s"Sequential reduction: $reduction")
    println(s"Parallel reduction: $parallelReduction")
  }

  def demoDefinedOrder(): Unit = {
    val strings = "I love parallel collections but i must be careful".split(" ").toList
    val concatenation = strings.reduce(_ + " " + _)
    val parallelConcatenation = strings.par.reduce(_ + " " + _)

    println(s"[SRL] $concatenation")
    println(s"[PLL] $parallelConcatenation") // this time is the same result as serial version, since the lambda we use to reduce is associative
  }

  // race conditions
  def demoRaceConditions(): Unit = {
    var sum = 0
    (1 to 1000).toList.par.foreach(elem => sum += elem)
    println(sum) // 500500 if sum is correct
    // is hard to get a correct result, since the lambda is running on several threads that can overlap on read step
    // causing race conditions between them
  }

  def main(args: Array[String]): Unit = {
   // compareListTransformation()
   // demoUndefinedOrder()
   // demoDefinedOrder()
    demoRaceConditions()
  }
}
