package com.rockthejvm.part5ts

object OpaqueTypes {

  object SocialNetwork {
    // some data structures = "domain" (all data types relevant for our business use-case)
    opaque type Name = String // opaque is for types definitions
    object Name  {
      def apply(str: String): Name = str
    }

    extension (name: Name)
      def lenght: Int = name.length // String API


    // inside, Name <-> String
    def addFriend(person1: Name, person2: Name): Boolean = person1.length == person2.length
  }
  // outside SocialNetwork Name and String are UNRELATED
  import SocialNetwork.*
  //val name: Name = "Daniel" // won't compile
  // why: you don't need/want to have access to the entire String API for the Name type

  object Graphics {
    opaque type Color = Int // in hex
    opaque type ColorFilter <: Color = Int


    val Red: Color = 0xFF000000
    val Green: Color = 0x00FF0000
    val Blue: Color = 0x0000FF00
    val halfTransparency: ColorFilter = 0x80 // 50%
  }

  import Graphics.*
  case class OverlayFilter(c: Color)

  val fadeLayer = OverlayFilter(halfTransparency) // ColorFilter <: Color

  // how can we create instances of opaque types / how to access their APIs
  // 1 - through companion objects defined in the scope of the definition of opaque type
  val aName: Name = Name("Daniel")
  // 2 - extension methods defined in the scope of the definition of opaque type
  val nameLength = aName.lenght

  def main(args: Array[String]): Unit = {

  }
}
