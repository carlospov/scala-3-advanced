package com.rockthejvm.part5ts

object VariancePositions {
  class Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal


  // 1 - type bounds
  // subtype bound
  class Cage[A <: Animal] // A must extend Animal
  //val aCage = new Cage[String] // wont compile bc String is not subtype of Animal
  val aCage = new Cage[Dog] // Ok bc Dog is subtype of Animal
  // supertype bound
  class WeirdContainer[A >: Animal] // Animal must extend Animal

  // 2 - variance positions
  //class Vet[-T](val favouriteAnimal: T) // error Contravariant type T occurs in covariant position in type T of value favouriteAnima
  /* if it compiled
  val garfield = new Cat
  val theVet: Vet[Animal] = new Vet[Animal](garfield)
  val aDogVet: Vet[Dog] = new Vet[Animal](....) in particular, we can use theVet
  val aDog: Dog = aDogVet.favouriteAnimal // must be a Dog - type conflict -> types of val fields are in COVARIANT position
   */

  // Types of var fields are also in covariant position
  // (same reason)

  // but here, types of var fields are in CONTRAVARIANT position
  // class MutableOption[+T](var contents: T) // error: Covariant type T occurs in contravariant position in type T of value contents
  // BOTH

  /*
  val maybeAnimal: MutableOption[Animal] = new MutableOption[Dog](new Dog)
  maybeAnimal.contents = new Cat // type conflict - cat is not a Dog
   */

  // --> var fields must be for INVARIANT types

  // types of method arguments are in CONTRAVARIANT position
//  class MyList[+T] {
//    def add(element: T): MyList[T] = ???
//  }
  /*
  val animals: MyList[Animal] = new MyList[Cat]
  val biggerListOfAnimals = animals.add(new Dog) // type conflict
   */

  // method return types
//  abstract class Vet2[-T] {
//    def rescueAnimal(): T // method returns T, and class "consumes" T, is a contradiction
//  }

  /*
  val vet: vet2[Animal] = new Vet2[Animal] {
    override def rescueAnimal(): Animal = new Cat
  }

  val lassiesVet: Vet2[Dog] = vet // Vet[Animal] substitutes Vet2[Dog]
  val rescueDog: Dog = lassiesVet.rescueAnimal() // will return Cat, that conflicts with the signature of the method rescueAnimal in Vet2[Dog]
   */

  /**
   * solving variance position problems
   * -> variance bounds
   */

  abstract class LList[+A] {
    def head: A
    def tail: LList[A]
    def add[B >: A](element: B): LList[B] // widen the type
  }
  // val animals: List [Cat] = list of cats
  // val newAnimals = animals.add(new Dog) -> new Animals must be List[Animal], a supertype of both

  class Vehicle
  class Car extends Vehicle
  class SuperCar extends Car
  class RepairShop[-A <: Vehicle] {
    def repair[B <: A](vehicle: B): B = vehicle // narrowing the type
  }

  val myRepairShop: RepairShop[Car] = new RepairShop[Vehicle]
  val mybeatupVW = new Car
  val freshCar = myRepairShop.repair(mybeatupVW) // works, returns a Car

  val damagedFerrari = new SuperCar
  val freshFerrari = myRepairShop.repair(damagedFerrari) // works, returns superCar







  def main(args: Array[String]): Unit = {

  }
}
