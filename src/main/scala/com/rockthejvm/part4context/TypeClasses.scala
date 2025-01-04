package com.rockthejvm.part4context

object TypeClasses {

  /*
  Small library  - to serialize some data to a standard format (HTML)
   */

  // V1 - the OO way
  trait HTMLWritable {
    def toHTML: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    override def toHTML: String = s"<div>$name ($age yo) <a href=$email/></div>"
  }

  val bobToHtml = User("Bob", 42, "bob@rockthejvm.com").toHTML
  // same for other data structures we want to serialize:

  /*
  Drawbacks:
  - only available for types written by ourselves
  - can only provide ONE implementation
   */

  // V2 - Pattern Matching (Scala Specific)
  object HTMLSerializerPM {
    def serializeToHTML(value: Any): String = value match {
      case User(name, age, email) => s"<div>$name ($age yo) <a href=$email/></div>"
      case _ => throw new IllegalArgumentException("data structure not supported")
    }
  }

  /*
  Drawbacks:
  - lost the type safety, we don't know if an object is serializable until code runs or throws an exception
  - need to modify a SINGLE piece of code every time
  - there's still just ONE implementation we can return
   */

  // V3 - type class: trait that is typed in some way
  // part 1 - type class definition
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }
  // part 2 - type class instances for every supported types
  given userSerializer: HTMLSerializer[User] with {
    override def serialize(value: User): String = {
      val User(name, age, email) = value
      s"<div>$name ($age yo) <a href=$email/></div>"
    }
  }

  val bob = User("Bob", 42, "bob@rockthejvm.com")
  val bobToHtml_v2 = userSerializer.serialize(bob)

  /*
  Benefits:
  - we can define serializers for other types OUTSIDE the "library"
  - we can define multiple serializers for the same type, and pick whichever we want
   */
  import java.util.Date
  given dateSerializer: HTMLSerializer[Date] with {
    override def serialize(value: Date): String = s"<div>${value.toString()}</div>"
  }

  val partialUserSerializer = new HTMLSerializer[User] {
    override def serialize(value: User): String = s"<div>${value.name}</div>"
  }
  //or if we want to organize givens properly..
//  object SomeOtherSerializerFunctionality {
//    given partialUserSerializer: HTMLSerializer[User] with {
//      override def serialize(value: User): String = s"<div>${value.name}</div>"
//    }
//  }

  // part 3- using the type class (user-facing API)
  object HTMLSerializer {
    def serialize[T](value: T)(using serializer: HTMLSerializer[T]): String =
      serializer.serialize(value)
    def apply[T](using serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }
  val bobToHtml_v3 = HTMLSerializer.serialize(bob)
  val bobToHtml_v4 = HTMLSerializer[User].serialize(bob)

  // part 4 - simplify by using extension methods
  object HTMLSyntax {
    extension [T](value: T)
      def toHtml(using serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }

  import HTMLSyntax.*
  val bobToHtml_v5 = bob.toHtml // same expressiveness as we had in the beginning but more flexible implementation
  // we can:
  /*
  - Add more instances of that type class in a different place than the definition of that type class
  - Support more data types independently of the type class definition
  - We can choose implementations by importing the right givens
  - more expressive via extension methods
   */



  def main(args: Array[String]): Unit = {
    println(bobToHtml == bobToHtml_v2)
    println(bobToHtml == bobToHtml_v3)
  }
}
