package com.rockthejvm.part5ts

object PathDependantTypes {

  class Outer {
    class Inner
    object InnerObject
    type InnerType

    def process(arg: Inner) = println(arg)
    def processGral(arg: Outer#Inner) = println(arg)

  }

  // every instance of outer will have its own types
  val outer = new Outer
  val inner = new outer.Inner // outer.Inner is a separate TYPE = path-dependant type

  val outer_A = new Outer
  val outer_B = new Outer

  //val inner_A: outer_A.Inner = new outer_B.Inner // won't compile
  val inner_A = new outer_A.Inner // these two are of different types
  val inner_B = new outer_B.Inner

  //

  //outer_A.process(inner_B) // won't compile, because process needs a Inner of this same instance of Outer
  outer.process(inner) // this is okey

  // parent-type: Outer#Inner (Inner type that depends on the class itself, not an instance of ir)
  // so I can say
  outer_A.processGral(inner_A)
  // AND
  outer_A.processGral(inner_B) // ok, outer_B.Inner <: Outer#Inner

  /*
  Why:
  - type-checking/type inference inside libraries (e.g Akka Streams)
  - type-level programming (Scala Macros?)
   */

  // methods with dependant types: return a different COMPILE-TIME type depending on the argument
  // without need for generics
  trait Record {
    type Key
    def defaultValue: Key
  }

  class StringRecord extends Record {
    override type Key = String
    override def defaultValue: String = ""
  }

  class IntRecord extends Record {
    override type Key = Int
    override def defaultValue: Int = 0
  }

  // user-facing API
  def getDefaultIdentifier(record: Record): record.Key = record.defaultValue

  val aString: String = getDefaultIdentifier(new StringRecord) // ok, a string
  val aInt: Int = getDefaultIdentifier(new IntRecord) // an int, ok

  // easy to find this above in libraries

  // function with dependant types (they rise from our need to use Higher Order Functions)
  val getIdentifierFunc: Record => Record#Key = getDefaultIdentifier // what kind of function do we have?
  // is a Function1[Record, Record#Key]

  def main(args: Array[String]): Unit = {

  }
}
