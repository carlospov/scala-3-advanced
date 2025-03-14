package com.rockthejvm.part5ts

import reflect.Selectable.reflectiveSelectable

object StructuralTypes {

  // structural type
  type SoundMaker = { // structural type
    def makeSound(): Unit
  }

  class Dog {
    def makeSound(): Unit = println("bark")
  }

  class Car {
    def makeSound(): Unit = println("vroom!")
  }

  val dog: SoundMaker = new Dog // ok
  val car: SoundMaker = new Car // ok --> Car is accepted as a substitute for SoundMaker bc it does the same as a SounMaker
  // compile-time duck typing: if acts like a duck and looks like a duck, it's a duck. Available in Scala through structural types

  // type refinements
  abstract class Animal {
    def eat(): String
  }

  type WalkingAnimal = Animal { // refined Type
    def walk(): Int
  } // a substitute for WalkingAnimal must extend Animal and has the walk method in its body

  // why do we need them??
  // creating type-safe API for existing types following the same structure but no connection to each other
  type JavaCloseable = java.io.Closeable
  class CustomCloseable {
    def close() = println("ok, ok I'm closing")
    def closeSilently() = println("not making sound") // additional methods
  }

  // we want something, an API, that can handle a standard java closable and the custom one
//  def closeResource(closeable: JavaCloseable | CustomCloseable): Unit =
//    closeable.close() // not ok, cannot be found

  // solution: structural type
  type UnifiedCloseable = {
    def close(): Unit
  }

  def closeResource(closeable: UnifiedCloseable): Unit = closeable.close()

  val jCloseable = new JavaCloseable {
    override def close(): Unit = println("closing Java resource")
  }

  val aCloseable = new CustomCloseable

  def closeResource_v2(closeable: { def close(): Unit}): Unit = closeable.close() // "Structural type definition" on the go


  def main(args: Array[String]): Unit = {
    dog.makeSound() // through reflection <--> inspect implementations AT RUNTIME --> very slow
    car.makeSound()

    closeResource(jCloseable)
    closeResource(aCloseable) // works
  }
}
