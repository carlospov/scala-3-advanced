package com.rockthejvm.part4context

// special import
import scala.language.implicitConversions

object ImplicitConversions {

  case class Person(name: String) {
    def greet(): String = s"Hi, I'm $name"
  }

  val daniel = Person("Daniel")
  val danielSaysHi = daniel.greet()

  // Person is just a wrapper of Strings, if we use them interchangeably, we would like to do so also in compiler type checks

  // once imported implicitConversions, there's the Conversion type (derivation of func1) with an apply method that must be implemented
  // and tells the compiler how to convert one type into another
  given string2person: Conversion[String, Person] with
    override def apply(x: String): Person = Person(x)

  val danielSaysHi_v2 = "Daniel".greet() // legal bc of the special conversion instance, the compiler does Person("Daniel").greet() whenever is needed

  def processPerson(person: Person): String =
    if (person.name.startsWith("J")) "OK"
    else "NOT OK"

  val isJaneOK = processPerson("Jane") // works bc Strings are converted to expected Person type by the compiler

  /* reasons this exist
  - auto-box types
  - use multiple types for the same code interchangeably
   */



  def main(args: Array[String]): Unit = {

  }
}
