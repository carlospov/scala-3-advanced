package com.rockthejvm.part4context

object ExtensionMethods {

  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name"
  }

  extension (string: String) {
    def greetAsPerson: String = Person(string).greet // for a string, using Person constructor we build one and call the greet method
  }

  val danielsGreeting = "Daniel".greetAsPerson

  // generic extension methods
  extension [A](list: List[A]) //we can enhance any list over the type A
    def ends: (A, A) = (list.head, list.last)
  // be careful, extension only takes 1 argument, you can only extend 1 instance with the extension method

  val aList = List(1,2,3,4)
  val firstandLast = aList.ends


  // reason #1: make APIs very expressive
  trait Combinator[A] {
    def combine(x: A, y: A): A
  }
  extension [A](list: List[A])
    def combineAll(using combinator: Combinator[A]): A =
      list.reduce(combinator.combine)

  // reason #2: enhance certain types with new capabilities
  // => Cats, ZIO, etc...powerful code
  given intCombinator: Combinator[Int] with
    override def combine(x: Int, y: Int): Int = x + y

  val firstSum = aList.combineAll // works, sum is 10
  val someStrings = List("I", "love", "Scala")
  // val stringsSum = someStrings.combineAll // doesn't work because there is no Combinator[String] in scope

  // grouping extensions under same braces
  object GroupedExtensions {
    extension [A](list: List[A]) {
      def ends: (A, A) = (list.head, list.last)
      def combineAll(using combinator: Combinator[A]): A =
        list.reduce(combinator.combine)
    }
  }

  // call extension methods directly
  val firstLast_v2 = ends(aList) // same as aList.ends

  /**
   * Exercises
   * 1. Add an isPrime to the Int type
   *    You should be able to write 7.isPrime
   *
   * 2. Add extensions to Tree:
   *    - map(f: A => B): Tree [B]
   *    - forall(predicate: A => Boolean): Boolean (&&)
   *    - sum => sum of all elements of the tree
   */

  // exercise 1
  extension (n: Int) {
//    def isPrime: Boolean = {
//      def primeUntil(s: Int): Boolean = {
//        if (s <= 1) true
//        else (n % s != 0) && primeUntil(s - 1)
//      }
//
//      primeUntil(n/2)
//    }// this is not a proper isPrime implementation since now Int goes from -inf to +inf
    def isPrime: Boolean = {
      def isPrimeAux(pd: Int): Boolean = {
        if (pd > n / 2) true
        else if (n % pd == 0) false
        else isPrimeAux(pd + 1)
      }

      assert(n >= 0)
      if (n == 0 || n == 1) false
      else isPrimeAux(2)
    }
  }

  val sevenisprime = 7.isPrime

  // exercise 2

  // "library code" == cannot change
  sealed abstract class Tree[A]
  case class Leaf[A](value: A) extends Tree[A]
  case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  extension [A](t: Tree[A]) {
    def map[B](f: A => B): Tree[B] = t match {
      case t: Leaf[A] => Leaf[B](f(t.value)) // can be cleaner: case Leaf(value) => Leaf(f(value)), etc. But while typing I forgot the match clause and had problems
      case t: Branch[A] => Branch[B](t.left.map(f), t.right.map(f)) // figuring out why didn't compile xd
    }
    def forall(predicate: A => Boolean): Boolean = t match {
      case t: Leaf[A] => predicate(t.value)
      case t: Branch[A] => t.left.forall(predicate) && t.right.forall(predicate)
    }
    def combineAll(using combinator: Combinator[A]): A = t match {
      case t: Leaf[A] => t.value
      case t: Branch[A] => combinator.combine(t.left.combineAll, t.right.combineAll)
    }
  }

  val IntTree: Tree[Int] = {
    Branch(Branch(Leaf(1),Leaf(5)), Leaf(6))
  }

  val sumOfTree = IntTree.combineAll



  def main(args: Array[String]): Unit = {
    println(danielsGreeting)
    println(firstandLast)
    println(firstSum)
    println(sevenisprime)
    println(sumOfTree)
  }
}
