package com.rockthejvm.part5ts

object TypeMembers {

  class Animal
  class Dog extends Animal
  class Cat extends Animal

  class AnimalCollection {
    // val, var, methods, class, trait, object
    type AnimalType // abstract Type member
    type BoundedAnimal <: Animal // abstract type member with a type bound
    type SuperBoundedAnimal >: Dog <: Animal
    type AnimalAlias = Cat // type alias
    type NestedOption = List[Option[Option[Int]]] // use case of type alias
  }

  class MoreConcreteAnimalCollection extends AnimalCollection {
    override type AnimalType = Dog // this implements the abstract type member
  }

  // using type members
  val ac = new AnimalCollection
  val anAnimal: ac.AnimalType = ???

  // val cat: ac.BoundedAnimal = new Cat // won't compile because BoundedAnimal is abstract type, not a concrete type, i't just a bounded abstract type
  // compiler can't type check

  // but you can say
  val aDog: ac.SuperBoundedAnimal = new Dog // compiles because Dog <: SuperBoundedAnimal
  val aCat: ac.AnimalAlias = new Cat // works because Cat == AnimalAlias

  // type aliases establish relationship between types
  // alternative to generics
  class LList[T] {
    def add(element: T): LList[T] = ???
  }


  class MyList {
    type T
    def add(element: T): MyList = ???
  }

  // .type
  type CatType = aCat.type // to surface type of any value
  val anewCat: CatType = aCat // works bc are the same type


  def main(args: Array[String]): Unit = {

  }
}
