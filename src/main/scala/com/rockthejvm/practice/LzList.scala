package com.rockthejvm.practice

// write a lazily evaluated potentially infinite linked list
abstract class LzList[A] {
  def isEmpty: Boolean
  def head: A
  def tail: LzList[A]

  // utilities
  def #::(element: A): LzList[A] // prepending
  infix def ++(another: => LzList[A]): LzList[A] // TODO warning

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
  infix def ++(another: => LzList[A]): LzList[A] = another
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
  override def #::(element: A): LzList[A] = new LzCons[A](element, this) // prepending

  infix def ++(another: => LzList[A]): LzList[A] = new LzCons[A](head, tail ++ another)

  // classics
  override def foreach(f: A => Unit): Unit = {
    def forEachTailrec(lzlist: LzList[A]): Unit = {
      if lzlist.isEmpty then ()
      else {
        f(lzlist.head)
        forEachTailrec(lzlist.tail)
      }
    }

    forEachTailrec(this)
  }

  override def map[B](f: A => B): LzList[B] = new LzCons[B](f(head), tail.map(f))
  override def flatMap[B](f: A => LzList[B]): LzList[B] = f(head) ++ tail.flatMap(f)
  override def filter(predicate: A => Boolean): LzList[A] =
    if (predicate(head)) new LzCons[A](head, tail.filter(predicate))
    else tail.filter(predicate) // TODO maybe dangerous, depending on predicate (look in main)

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

  def from[A](list: List[A]): LzList[A] = list.reverse.foldLeft(LzList.empty) { (currentLzList, newElement) =>
    new LzCons(newElement, currentLzList)
  }

  def apply[A](values: A*) = LzList.from(values.toList)

  def fibonacci: LzList[BigInt] = {
    def fibo(first: BigInt, second: BigInt): LzList[BigInt] =
      new LzCons[BigInt](first, fibo(second, first + second))

    fibo(1, 1)
  }

  def eratosthenes: LzList[Int] = {

    def isPrime(n: Int) = {
      def isPrimeTailrec(divisor: Int): Boolean = {
        if (divisor < 2) true
        else if (n % divisor == 0) false
        else isPrimeTailrec(divisor - 1)
      }

      isPrimeTailrec(n / 2)
    }

    def sieve(numbers: LzList[Int]): LzList[Int] = {
      if (numbers.isEmpty) numbers
      else if (!isPrime(numbers.head)) sieve(numbers.tail)
      else new LzCons[Int](numbers.head, sieve(numbers.tail.filter(_ % numbers.head != 0)))
    }

    val naturals = LzList.generate(2)(_ + 1)
    sieve(naturals)

  }
}
object LzListPlayground {
  def main(args: Array[String]): Unit = {
    val naturals = LzList.generate(1)(n => n + 1) // should be the infinite list of natural numbers
    println(naturals.head) // 1
    println(naturals.tail.head) // 2
    println(naturals.tail.tail.head) // 3

    val first50k =  naturals.take(50000)
    // first50k.foreach(println)
    // val first50kList = first50k.toList
    // println(first50kList)

    // classics
    println(naturals.map(_ * 2).takeAsList(100))
    println(naturals.flatMap(x => LzList(x, x + 1)).takeAsList(100))
    // println(naturals.filter(_ < 10).takeAsList(10)) // crash with SO o infinite recursion

    val combinationsLazy: LzList[String] = for {
      number <- LzList(1, 2, 3)
      string <- LzList("Black", "White")
    } yield s"$number-$string"
    println(combinationsLazy.toList)

    // primes
    val primes = LzList.eratosthenes

    println(primes.takeAsList(100))
  }
}
