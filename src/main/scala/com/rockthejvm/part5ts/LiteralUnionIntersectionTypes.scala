package com.rockthejvm.part5ts

object LiteralUnionIntersectionTypes {

  // 1 - literal types
  val anumber = 3
  val three: 3 = 3 // literal type 3, 3 <: Int

  def passNumber(n: Int) = println(n)
  passNumber(45) // ok
  passNumber(three) // ok

  def passTrict(n: 3) = println(n)
  passTrict(three) // ok
  passTrict(3) // ok
  // but not
  //passTrict(45) // not ok, 45 not <: 3

  // available for double, string, Boolean
  val pi: 3.14 = 3.14
  val truth: true = true
  val favLang: "Scala" = "Scala"

  def doSomethingWithYourLife(meaning: Option[42]) = meaning.foreach(println)

  // 2 - union types
  val truthor42: Boolean | Int = 43 // Boolean OR Int

  def ambivalentMEthod(arg: String | Int) = arg match {
    case _: String => "a String"
    case _: Int => "a Number"
  } // Pm complete

  val aNumber = ambivalentMEthod(56) // ok
  val aString = ambivalentMEthod("Scala") // ok

  // type inference
  val stringOrInt = if (43 > 0) "a string" else 45 // type inference is Any, it chooses the lowest common ancestor of the two possible types
  // instead of String | Int, but can be forced
  val stringOrInt_v2: String | Int = if (43 > 0) "a string" else 45 // ok

  // union types + nulls
  type Maybe[T] = T | Null // not null, but the Null type
  def handleMaybe(someValue: Maybe[String]): Int =
    if (someValue != null) someValue.length // flow typing, a feature of the compiler that inferes type as the code "flows", not only based on types
    else 0

  // here compiler is not so smart
//  type ErrorOr[T] = T | "error"
//  def handleResource(arg: ErrorOr[Int]): Unit =
//    if (arg != "error") println(arg + 1)
//    else println("Error!")


  // Intersection types
  class Animal
  trait Carnivore
  class Crocodile extends Animal with Carnivore
  val carnivoreAnimal: Animal & Carnivore = new Crocodile // ok

  trait Gadget {
    def use(): Unit
  }

  trait Camera extends Gadget {
    def takePicture() =  println("Smile")
    override def use() = println("snap")
  }

  trait Phone extends Gadget {
    def makePhoneCall() = println("Calling...")
    override def use() = println("ring")
  }

  def useSmartDevice(sp: Camera & Phone): Unit = {
    sp.takePicture() // both methods are available
    sp.makePhoneCall()
    sp.use() // what method will be used??? Depends on which extends which in the class of the object sp
  }

  class SmartPhone extends Camera with Phone // if done like this, we need the use() methods to be overriding a use() base method
  // (diamond problem)
  class SmartPhone_v2 extends Phone with Camera

  // intersection types + covariance
  trait HostConfig
  trait HostController {
    def get: Option[HostConfig]
  }

  trait PortConfig
  trait PortController {
    def get: Option[PortConfig]
  }

  def getConfig(controller: HostController & PortController) = controller.get // what is the result of this??

//  def getConfig(controller: HostController & PortController): Option[HostConfig] & Option[PortConfig] = controller.get // the return type must be an intersection of two possible return types
//  def getConfig(controller: HostController & PortController): Option[HostConfig & Option[PortConfig] = controller.get // Option is covariant, so this is the same as above

  def main(args: Array[String]): Unit = {
    useSmartDevice(new SmartPhone)
    useSmartDevice(new SmartPhone_v2)
  }
}
