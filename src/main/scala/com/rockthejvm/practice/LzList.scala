package com.rockthejvm.practice

// write a lazily evaluated potentially infinite linked list
abstract class LzList[A] {
  def isEmpty: Boolean
  def head: A
  def tail: LzList[A]

  // utilities
  def #::(element: A): LzList[A] // prepending
  def ++(another: LzList[A]): LzList[A] // TODO warning

  // classics
  def foreach(f: A => Unit): Unit
  def map[B](f: A => B): LzList[B]
  def flatMap[B](f: A => LzList[B]): LzList[B]
  def filter(predicate: A => Boolean): LzList[A]
  def withFilter(predicate: A => Boolean): LzList[A] = filter(predicate)

  def take(n: Int): LzList[A] // takes the first n elements from this lazy list ( Scala 2.12 called this "Streams")
  def takeAsList(n: Int): List[A] =
    take(n).toList

  def toList: List[A] = {
    def toListAux(remaining: LzList[A], acc: List[A]): List[A] =
      if (remaining.isEmpty) acc.reverse
      else toListAux(remaining.tail, remaining.head :: acc)

    toListAux(this, List())
  }// dangerous, use carefully on large lazy lists
}

case class LzEmpty[A]() extends LzList[A] {
  override def isEmpty: Boolean = true
  override def head: A = throw new NoSuchElementException()
  override def tail: LzList[A] = throw new NoSuchElementException()
  // utilities
  override def #::(element: A): LzList[A] = new LzCons[A](element, this)// prepending
  override def ++(another: LzList[A]): LzList[A] = another
  // classics
  override def foreach(f: A => Unit): Unit = ()
  override def map[B](f: A => B): LzList[B] = LzEmpty[B]()
  override def flatMap[B](f: A => LzList[B]): LzList[B] = LzEmpty[B]()
  override def filter(predicate: A => Boolean): LzList[A] = this

  override def take(n: Int): LzList[A] =
    if (n == 0) this
    else throw new RuntimeException(s"Cannot take $n elements from an empty lazy list")// takes the first n elements from this lazy list ( Scala 2.12 called this "Streams")
}

class LzCons[A](hd: => A, tl: => LzList[A]) extends LzList[A] {
  override def isEmpty: Boolean = false

  // hint: use call by need
  override lazy val head: A = hd
  override lazy val tail: LzList[A] = tl

  // utilities
  override def #::(element: A): LzList[A] = new LzCons[A](element, this)// prepending
  override def ++(another: LzList[A]): LzList[A] = new LzCons[A](head, tail ++ another) // TODO warning
  // classics
  override def foreach(f: A => Unit): Unit =
    f(head)
    tail.foreach(f)

  override def map[B](f: A => B): LzList[B] = new LzCons[B](f(head), tail.map(f))
  override def flatMap[B](f: A => LzList[B]): LzList[B] = f(head) ++ tail.flatMap(f)
  override def filter(predicate: A => Boolean): LzList[A] =
    if (predicate(head)) new LzCons[A](head, tail.filter(predicate))
    else tail.filter(predicate) // TODO maybe dangerous, depending on predicate

  override def take(n: Int): LzList[A] =
    if (n <= 0) LzEmpty[A]()
    else if (n == 1) LzCons[A](head, LzEmpty[A]())
    else new LzCons[A](head, tail.take(n - 1))// takes the first n elements from this lazy list ( Scala 2.12 called this "Streams")
}

object LzList {
  def empty[A]: LzList[A] = LzEmpty[A]()
  
  def generate[A](start: A)(generator: A => A): LzList[A] = {
    new LzCons(start, LzList.generate(generator(start))(generator))
  }

  def from[A](list: List[A]): LzList[A] = list.foldLeft(LzList.empty) { (currentLzList, newElement) => 
    new LzCons(newElement, currentLzList)
  }
}
object LzListPlayground {
  def main(args: Array[String]): Unit = {
    val naturals = LzList.generate(1)(n => n + 1) // should be the infinite list of natural numbers
  }
}
