package com.rockthejvm.part4context

object Implicits {

  // legacy - old way of doing
  // given/using clauses  ->> the ability to pass arguments automatically (implicitly) by the compiler
  trait SemiGroup[A] {
    def combine(x: A, y: A): A
  }

  // scala 3
//  def combineAll[A](list: List[A])(using semigroup: SemiGroup[A]): A=
//    list.reduce(semigroup.combine)
//
//  given intSemigroup: SemiGroup[Int] with
//    override def combine(x: Int, y: Int): Int = x + y
//
//  val sumof10 = combineAll((1 to 10).toList)
//
  // old way of doing it
  def combineAll[A](list: List[A])(implicit semigroup: SemiGroup[A]): A=
    list.reduce(semigroup.combine)

  implicit val intSemigroup: SemiGroup[Int] = new SemiGroup[Int] {
    override def combine(x: Int, y: Int): Int = x + y
  }
  // implicit arg --> using clause
  // implicit val --> given declaration

  val sumof10 = combineAll((1 to 10).toList)


  // extension methods == implicit classes

  // Scala 3
  // 23.isEven is now possible through extension methods
//  extension (number: Int) // <-- note that here we don't need to specify a new classname anymore
//    def isEven = number % 2 == 0
//
//  val is23Even = 23.isEven
//

  // old way in Scala 2
  implicit class MyRichInteger(number: Int) {
    // expansion methods here
    def isEven = number % 2 == 0
  }

  val is23Even = 23.isEven // new MyRichInteger(23).isEven

  // implicit classes --> extension methods/zones

  // implicit conversions - SUPER DANGEROUS IN THIS WAY
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  implicit def String2Person(string: String): Person =   // this tells the compiler that it is free to rewrite code whenever I need a Person and provide a String instead
    Person(string)

  val danielSaysHi = "Daniel".greet() // String2Person("Daniel").greet()

  // implicit def => synthesize NEW implicit values
  implicit def semigroupOfOption[A](implicit semigroup: SemiGroup[A]): SemiGroup[Option[A]] = new SemiGroup[Option[A]] {
    override def combine(x: Option[A], y: Option[A]): Option[A] = for {
      valueX <- x
      valueY <- y
    } yield semigroup.combine(valueX, valueY)
  }

  // in Scala 3
//  given semigroupOfOption[A](using semigroup: SemiGroup[A]): SemiGroup[Option[A]] with {
//    override def combine(x: Option[A], y: Option[A]): Option[A] = for {
//      valueX <- x
//      valueY <- y
//    } yield semigroup.combine(valueX, valueY)
//  }

  // organizing implicits == organizing contextual abstractions
  // but...
  // import package.* // also imports implicits <-- only practical difference

  /*
  Why implicits will be phased out:
  - the implicit keyword had too many different meanings
  - conversions are easy to abuse
  - implicits are very hard to track down while debugging (givens aren't trivial, but they're explicitly imported)
   */


  /* WAY TO DO CONTEXTUAL ABSTRACTIONS:
  - use given/using clauses
  - extension methods
  - explicitly declared implicit conversions
  */

  def main(args: Array[String]): Unit = {
    println(sumof10)
  }
}
