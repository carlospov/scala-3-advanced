package com.rockthejvm.part5ts

object Variance {

  class Animal
  class Dog(name: String) extends Animal

  // variance question for List:
  // If Dog extends Animal then should a List[Dog] "extend" List[Animal]??
  //
  // 1) for List the answer is Yes -> List is COVARIANT
  //
  val lassie = new Dog("Lassie")
  val hachi = new Dog("Hachi")
  val laika = new Dog("Laika")

  val anAnimal: Animal = lassie // ok, dog <: Animal
  val myDogs: List[Animal] = List(lassie, hachi, laika) // ok - list is COVARIANT so a list of dogs is a list of animals

  // to make that, we have to add something to the Generic type List (or whatever generic we implement), a + marker
  // that is, List is really List[+A], that makes it covariant in A

  class MyList[+A] // MyList is COVARIANT in A
  val aListOfAnimals: MyList[Animal] = new MyList[Dog] //ok

  // there are types for which it don't make sense to add covariance
  // 2) if DOESN'T then the type is INVARIANT
  trait Semigroup[A] { // no marker means that is INVARIANT, meaning that in general you cant substitute one Semigroup of
    // one type for another Semigroup of a type that extends the first
    def combine(x: A, y: A): A
  }

  // java generics
  // val aJavaList: java.util.ArrayList[Animal] = new java.util.ArrayList[Dog] // won't compile, there's a type mismatch:
  // java generics are ALL invariant

  // 3) if HELL NO -> CONTRAVARIANCE
  // if Dog <: Animal ---> Vet[Animal] <: Vet[Dog]
  trait Vet[-A] { // this Vet type is contravariant in A
    def heal(animal: A): Boolean
  }


  val myVet: Vet[Dog] = new Vet[Animal] {
    override def heal(animal: Animal): Boolean = {
      println("Hey, all good")
      true
    }
  }

  val healLaika = myVet.heal(laika) // ok

  /*
  How do we decide??
  - if your type produces or retrieves a value (e.g a List) then should be COVARIANT
  - if your type ACTS ON / CONSUMES a value (e.g Vet) then it should be CONTRAVARIANT
  - otherwise, INVARIANT
   */

  /**
   * Exercises
   */

  // 1 - which types should be invariant, covariant or contravariant?
  class RandomGenerator[+A]
  class MyOption[+A] // similar to Option[A]
  class JSONSerializer[-A] // consumes values and turn them into strings
  trait MyFunction[-A, +B] // similar to Function1[A, B] // consumes type A and produces type B

  // 2 - add variance modifiers to this "library"
  abstract class LList[+A] {
    def head: A
    def tail: LList[A]
  }

//  case class EmptyList[+A]() extends LList[A] {
//    override def head = throw new NoSuchElementException()
//    override def tail = throw new NoSuchElementException()
//  }
  case object EmptyList extends LList[Nothing] {
      override def head = throw new NoSuchElementException()
      override def tail = throw new NoSuchElementException()
  }

  case class Cons[+A](override val head: A, override val tail: LList[A]) extends LList[A]

  val aList: LList[Int] = EmptyList // fine
  val anotherList: LList[String] = EmptyList // also fine

  // Nothing <: A, then LList[Nothing] <: LList[A]

  def main(args: Array[String]): Unit = {

  }
}
