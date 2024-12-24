package com.rockthejvm.practice

abstract class FSet [A] extends (A => Boolean) {
  // main api
  def contains(elem: A): Boolean
  override def apply(elem: A): Boolean = contains(elem)

  // utility methods to interact with other sets or with elements
  infix def +(elem: A): FSet[A]
  infix def ++(anotherSet: FSet[A]): FSet[A]

  // "classics"
  def map[B](f: A => B): FSet[B]
  def flatMap[B](f: A => FSet[B]): FSet[B]
  def filter(predicate: A => Boolean): FSet[A]
  def foreach(f: A => Unit): Unit

  // methods
  infix def -(elem: A): FSet[A]
  infix def --(anotherSet: FSet[A]): FSet[A]
  infix def &(anotherSet: FSet[A]): FSet[A]

  // "negation" == all the elements of type A except the elements in this set
  def unary_! : FSet[A] = new PBSet[A](x => !contains(x))
}

// example { x in N | x % 2 == 0 }
// property-based set
class PBSet[A](property: A => Boolean) extends FSet[A] {
  override def contains(elem: A): Boolean = property(elem)

  override infix def +(elem: A): FSet[A] = new PBSet[A](x => x == elem | property(x))
  override infix def ++(anotherSet: FSet[A]): FSet[A] = new PBSet[A](x => property(x) | anotherSet(x))

  override def map[B](f: A => B): FSet[B] = throw new RuntimeException("I don't know if this set is iterable")
  override def flatMap[B](f: A => FSet[B]): FSet[B] = throw new RuntimeException("I don't know if this set is iterable")
  override def filter(predicate: A => Boolean): FSet[A] = PBSet[A](x => property(x) && predicate(x))
  override def foreach(f: A => Unit): Unit = throw new RuntimeException("I don't know if this set is iterable")

  override def -(elem: A): FSet[A] = filter(x => x != elem)
  override def --(anotherSet: FSet[A]): FSet[A] = filter(!anotherSet)
  override def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet)
}

case class Empty[A]() extends FSet[A] { // we can implement it also as PBSet(x => false)
  override def contains(elem: A): Boolean = false

  override infix def +(elem: A): FSet[A] = Cons[A](elem, Empty())
  override infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet

  override def map[B](f: A => B): FSet[B] = Empty[B]()
  override def flatMap[B](f: A => FSet[B]): FSet[B] = Empty[B]()
  override def filter(predicate: A => Boolean): FSet[A] = this
  override def foreach(f: A => Unit): Unit = ()

  override def -(elem: A): FSet[A] = this
  override def --(anotherSet: FSet[A]): FSet[A] = this
  override def &(anotherSet: FSet[A]): FSet[A] = this
}

case class Cons[A](head: A, tail: FSet[A]) extends FSet[A] {
  override def contains(elem: A): Boolean = elem == head || tail.contains(elem)

  override infix def +(elem: A): FSet[A] = {
    if (this.contains(elem)) this
    else Cons(elem, this)
  }
  override infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet ++ tail + head
  override def map[B](f: A => B): FSet[B] = tail.map(f) + f(head)
  override def flatMap[B](f: A => FSet[B]): FSet[B] = tail.flatMap(f) ++ f(head) // order of elements is not necessarily preserved
  override def filter(predicate: A => Boolean): FSet[A] = {
    val filteredTail = tail.filter(predicate)
    if predicate(head) then filteredTail + head
    else filteredTail
  }
  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  override def -(elem: A): FSet[A] = {
    if (head == elem) tail
    else tail - elem + head
  }
  override def --(anotherSet: FSet[A]): FSet[A] = filter(x => !anotherSet(x)) // set as function
  //override def --(anotherSet: FSet[A]): FSet[A] = anotherSet -- tail - head <-- my original implementation
  override def &(anotherSet: FSet[A]): FSet[A] = filter(anotherSet) // <- set as a function, when applied, set is filtered by a function mapping elems in another set to boolean
  // override def &(anotherSet: FSet[A]): FSet[A] = this -- (this -- anotherSet) <-- my original implementation
 }


object FSet {
  def apply[A](values: A*): FSet[A] = {
    def buildSet(valuesSeq: Seq[A], acc: FSet[A]): FSet[A] = {
      if (valuesSeq.isEmpty) acc
      else buildSet(valuesSeq.tail, acc + valuesSeq.head)
    }

    buildSet(values, Empty())
  }
}

object FunctionalSetPlayground {

  val aSet = Set(1,2,3)



  def main(args: Array[String]): Unit = {
    val first5 = FSet(1,2,3,4,5)
    val someNumbers = FSet(4,5,6,7,8)
    println(first5.contains(5)) // true
    println(first5(6)) // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1)) // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7)) // false

    println((first5 - 3).contains(3)) // false
    println((first5 -- someNumbers).contains(4)) // false
    println((first5 & someNumbers).contains(4)) // true

    val naturals = new PBSet[Int](_ => true)
    println(naturals.contains(345678)) // true
    println(!naturals.contains(0)) // false
    println((!naturals + 1 + 2 + 3).contains(3)) // true
    // println(!naturals.map(_ + 1)) // throw because map is ill defined in property-defined sets
  }
}
