package com.rockthejvm.part4context

object OrganizingCAs {

  val aList = List(2,3,1,4)
  val orderedList = aList.sorted

  // compiler fetches givens/EMs
  // 1st- local scope
  given reverseOrdering: Ordering[Int] with
    override def compare(x: Int, y: Int): Int = y - x

  // 2nd- imported scope
  case class Person(name: String, age: Int)
  val persons = List(Person("Alice", 29), Person("Sarah", 21), Person("Nil", 9), Person("Bob", 31))

  object PersonGivens {
    // this is "other place", is not in the scope where the sorted is being called
    given ageOrdering: Ordering[Person] with
      override def compare(x: Person, y: Person): Int = y.age - x.age

    extension (p: Person)
      def greet(): String = s"Hey, I'm ${p.name}. Glad to meet you!"
  }


  // a- import explicitly
  //import PersonGivens.ageOrdering
  // b- import a given for a particular type
  //import PersonGivens.{given Ordering[Person]} // there can only be one object of this type
  // c- import all givens
  //import PersonGivens.given
  // warning: import PersonGivens.* does NOT import given instances

  // 3rd- companion of all types involved in method signature
  //   override def sorted[B >: A](using ord: Ordering[B]): List[C]    (for out use case)
  /*
  Compiler will look in companion objects of:
  - Ordering
  - List
  - Person
   */
  object Person {
    given byNameOrdering: Ordering[Person] with
      override def compare(x: Person, y: Person): Int =
        x.name.compareTo(y.name)

    extension (p: Person)
      def greet(): String = s"Hello, I'm ${p.name}"
  }

  val sortedPersons = persons.sorted


  /*
  Good practice tips
  1) When a default given works in 99% cases, put that in the companion object of the type
  2) When multiple given instances make sense but one is dominant, put the dominant one in the companion object of the type
    and the rest in separate objects that can be imported independently
  3) If there's no a 'best one' put all in different objects
   */

  // same principles apply for extension methods, because they're searched by the compiler in the same order it does for given instances

  /**
   * Exercises. Create given instances for Ordering[Purchase]
   * - ordering by total prices (nunits*unitprice), descending = 50% of code base
   * - ordering by unit count, descending = 25% of code base
   * - ordering by unit price, ascending = 25% of code base
   *
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    given TotalPriceOrdering: Ordering[Purchase] with
      override def compare(x: Purchase, y: Purchase): Int =
        val xTotal = x.nUnits * x.unitPrice
        val yTotal = y.nUnits * y.unitPrice
        yTotal.compareTo(xTotal)
  }

  object PurchaseUCOrdering {
    given UnitCountOrdering: Ordering[Purchase] with
      override def compare(x: Purchase, y: Purchase): Int =
        y.nUnits.compareTo(x.nUnits)
    // or like...
    //given UnitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((x, y) => y.nUnits > x.nUnits)
  }
  object PurchaseUPOrdering {
    given UnitPriceOrdering: Ordering[Purchase] with
      override def compare(x: Purchase, y: Purchase): Int =
        x.unitPrice.compareTo(y.unitPrice)
    // or like...
    //given UnitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((x, y) => x.unitPrice > y.unitPrice)
  }

  val purchaseList = List(
    Purchase(4, 5.6),
    Purchase(1, 0.6),
    Purchase(4, 3.6),
    Purchase(2, 5.0),
    Purchase(3, 2.1),
    Purchase(5, 2.9),
    Purchase(9, 0.1)
  )

  val totalOrderedPurchase = purchaseList.sorted


  def main(args: Array[String]): Unit = {
    println(orderedList) // 1,2,3,4   or 4,3,2,1 if reverseOrdering is selected as implicit ordering
    println(sortedPersons)
    // println(Person("Daniel",99).greet()) // this will print "Hello, I'm ${p.name}"
    import PersonGivens.* // this DOES include extension methods
    println(Person("Daniel",99).greet())

    println(totalOrderedPurchase)
    import PurchaseUPOrdering.given
    println(purchaseList.sorted)


  }
}
