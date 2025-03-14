package com.rockthejvm.part5ts

object FBoundedPolymorphism {

  object Problem {
    trait Animal {
      def breed: List[Animal]
    }

    class Cat extends Animal {
      override def breed: List[Animal] = List(new Cat, new Dog) // we can breed a Dog from a Cat --> type safety is lost
    }

    class Dog extends Animal {
      override def breed: List[Animal] = List(new Dog,new Dog,new Dog)
    }

  }

  object NaiveSolution {
    trait Animal {
      def breed: List[Animal]
    }

    class Cat extends Animal {
      override def breed: List[Cat] = List(new Cat, new Cat) // solved
    }

    class Dog extends Animal {
      override def breed: List[Dog] = List(new Dog,new Dog,new Dog)
    }

    // but I HAVE to write proper type signatures
  }

  // I don't wanna rely on myself: I want the compiler to help me

  // F-Bounded Polymorphism (FBP)

  object FBP {
    trait Animal[A <: Animal[A]] { // recursive-type or F-Bounded Polymorphism
      def breed: List[Animal[A]]
    }

    class Cat extends Animal[Cat] {
      override def breed: List[Animal[Cat]] = List(new Cat, new Cat)
    }

    class Dog extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = List(new Dog, new Dog)
    }

    // now types can't be mistaken,
    // although it can be messed up, it requires some negligence
    class Crocodile extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = ??? // list of  Dogs ( but it is bc Crocodile it's just a name, it is extending a Dog)
    }

  }

  // example: some Object Relational Mapping libraries
  trait Entity[E <: Entity[E]]

  // example 2: Java sorting library
  class Person extends Comparable[Person] { // FBP
    override def compareTo(o: Person): Int = ???
  }

  // FBP + self-types
  object FPBSelf {
    trait Animal[A <: Animal[A]] { self: A =>
      def breed: List[Animal[A]]
    }

    class Cat extends Animal[Cat] { // Cat is IDENTICAL to Animal[Cat]
      override def breed: List[Animal[Cat]] = List(new Cat, new Cat)
    }

    class Dog extends Animal[Dog] {
      override def breed: List[Animal[Dog]] = List(new Dog, new Dog)
    }

    // now can't mess up
//    class Crocodile extends Animal[Dog] {
//      override def breed: List[Animal[Dog]] = ??? // Doesn't compile bc it doesn't match with self-type requirement
//    }

    // We can try to mess up deeper
    trait Fish extends Animal[Fish]
    class Cod extends Fish {
      override def breed: List[Animal[Fish]] = List(new Cod, new Cod)
    }

    class Shark extends Fish {
      override def breed: List[Animal[Fish]] = List(new Cod) // this is fine for the compiler
    }

    // solution level 2
    trait FishL2[A <: FishL2[A]] extends Animal[FishL2[A]] { self: A => }
    class Tuna extends FishL2[Tuna] {
      override def breed: List[Animal[FishL2[Tuna]]] = List(new Tuna)
    }

//    class Swordfish extends FishL2[Swordfish] {
//      override def breed: List[Animal[FishL2[Swordfish]]] = List(new Tuna) // this is not okay
//    }



  }



  def main(args: Array[String]): Unit = {

  }
}
