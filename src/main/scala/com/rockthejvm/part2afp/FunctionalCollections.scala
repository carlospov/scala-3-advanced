package com.rockthejvm.part2afp

import scala.collection.parallel.mutable.ParHashMap

object FunctionalCollections {

  // sets are functions
  val aSet: Set[String] = Set("I", "love", "Scala")
  val setContainsScala = aSet.contains("Scala") // true
  val setContainsScala_v2 = aSet("Scala") // true bc sets are functions to booleans

  // Seq are functions(from index: Int to elements: A) but not necessarily total functions
  val aSeq: Seq[Int] = Seq(1, 2, 3, 4)
  val anElement = aSeq(2) // element 3
  val aNonExisting = aSeq(8) // will not return false, it will throw exception (OOBException)
  // Seqs are PartialFunctions[Int, A]

  // Map[K, V] (data structure) "extends" PartialFunction[K, V]
  val aPhoneBook: Map[String, Int] = Map(
    "Alice" -> 123456,
    "Bob" -> 978654,
  )
  val alicesPhonenumber = aPhoneBook("Alice")
  // val MyPhoneNumber = aPhoneBook("Carlos") // throw a NoSuchElementException

  def main(args: Array[String]): Unit = {
    // println(aNonExisting)
  }
}
