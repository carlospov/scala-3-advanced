package com.rockthejvm.part5ts

object SelfTypes {

  trait Instrumentalist {
    def play(): Unit
  }

  trait Singer { self: Instrumentalist => // self-type : marker to the compiler that says that whoever class that implements singer must also implement Instrumentalist
    //            ^^ name --> can be anything, usually called "self"
    // DO NOT confuse with lambdas, nothing to do

// rest of the trait API

    def sing(): Unit
  }

  class LeadSinger extends Singer with Instrumentalist {
    override def sing(): Unit = ???
    override def play(): Unit = ???
  }

//  class Vocalist extends Singer { // won't compile: illegal inheritance --> not okay because it's not extending Instrumentalist
//
//  }

  val jamesHetfield = new Singer with Instrumentalist: // extend on the spot
    override def sing(): Unit = ???
    override def play(): Unit = ???

  class Guitarist extends Instrumentalist {
    override def play(): Unit = println("some guitar solo")
  }

  val ericCalpton = new Guitarist with Singer: // also okay because Guitarist derives from Instrumentalist
    override def sing(): Unit = println("layla")

  // self-types vs Inheritance
  class A
  class B extends A // B "is an" A

  trait T
  trait S { self: T => } // S "requires" a T  ~ "whatever ends up being a" S "must also be a" T

  // use cases: a design pattern ---> "cake pattern"
  abstract class Component {
    // main general API here
  }

  class ComponentA extends Component
  class ComponentB extends Component

  // in regular dependency injection
  class DependantComponent(val component: Component) // regular dependency injection

  // cake pattern
  trait ComponentLayer1 {
    // API
    def actionLayer1(x: Int): String
  }

  trait ComponentLayer2 { self: ComponentLayer1 =>
    // some other APIs
    def actionLayer2(x: String): Int
  }

  trait Application { self: ComponentLayer1 with ComponentLayer2 =>
    // your main API
  }


  // example: a photo taking application API in style of Instagram
  // layer 1 - small components
  trait Picture extends ComponentLayer1
  trait Stats extends ComponentLayer1

  // layer 2 - compose of level 1 components
  trait ProfilePage extends ComponentLayer2 with Picture
  trait Analytics extends ComponentLayer2 with Stats

  // layer 3 - main application
  trait AnalyticsApp extends Application with Analytics

  // dependencies are specified in layers, like baking cake
  // when you put the pieces together, you can pick a possible implementation from each layer

  // self-types to hide or preserve the "this" instance
  class SingerWithInnerClass { self => // self-type with no type reference, self == this
    class Voice {
      def sing() = this.toString // this == the voice, otherwise it refers to the upper class. Use self.this for the upper one
    }
  }

  // cyclical inheritance does not work
//  class X extends Y // <-- won't compile
//  class Y extends X

  // cyclical dependencies, this below can happen at the same time
  trait X { self: Y => }
  trait Y { self: X => }
  trait Z extends X with Y // requirements satisfied



  def main(args: Array[String]): Unit = {

  }
}
