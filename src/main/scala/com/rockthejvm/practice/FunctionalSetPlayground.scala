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
}

case class Empty[A]() extends FSet[A] {
  override def contains(elem: A): Boolean = false

  override infix def +(elem: A): FSet[A] = Cons[A](elem, Empty())
  override infix def ++(anotherSet: FSet[A]): FSet[A] = anotherSet

  override def map[B](f: A => B): FSet[B] = Empty[B]()
  override def flatMap[B](f: A => FSet[B]): FSet[B] = Empty[B]()
  override def filter(predicate: A => Boolean): FSet[A] = this
  override def foreach(f: A => Unit): Unit = ()
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
    println(first5.contains(5)) // true
    println(first5(6)) // false
    println((first5 + 10).contains(10)) // true
    println(first5.map(_ * 2).contains(10)) // true
    println(first5.map(_ % 2).contains(1)) // true
    println(first5.flatMap(x => FSet(x, x + 1)).contains(7)) // false

  }
}
