package com.rockthejvm.practice

import java.util.Date

object JSONSerialization {

  /*
  Users, posts, feeds (collection of blogposts for given user(s))
  Serialize to JSON
   */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  /*
  Steps:
    1- intermediate data: numbers, strings, lists, dates, objects
    2- type class to convert any data type to intermediate data
    3- serialize to JSON
   */

  // 1 - first step
  sealed trait JSONValue {
    def stringify: String
  }

  final case class JSONString(value: String) extends JSONValue {
    override def stringify: String = "\"" + value + "\""
  }

  final case class JSONNumber(value: Int) extends JSONValue {
    override def stringify: String = value.toString
  }

  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    override def stringify: String = values.map(_.stringify).mkString("[", ",", "]") // ["string", number, ...]
  }

  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    /* JSON Objects are this kind of general structure
    {
      "name": "John",
      "age": 22,
      "friends": [...],
      "latestPost": {...}
    }
     */
    override def stringify: String = values.map {
      case (k,v) => "\"" + k + "\":" + v.stringify
    }.mkString("{",",","}")
  }

  val data = JSONObject(Map(
    "user" -> JSONString("Daniel"),
    "posts" -> JSONArray(List(
      JSONString("Scala is awesome"),
      JSONNumber(42)
    ))
  ))

  // step 2 - type class pattern
  // step 2.1 - type class definition
  trait JSONConverter[A] {
    def convert(value: A): JSONValue
  }
  // step 2.2 - TC instances for User, Post, Feed and every data type they depend on: Int, Date, String
  given stringConverter: JSONConverter[String] with
    override def convert(value: String): JSONValue = JSONString(value) // JSONString takes a string

  given intConverter: JSONConverter[Int] with
    override def convert(value: Int): JSONValue = JSONNumber(value)

  given dateConverter: JSONConverter[Date] with
    override def convert(value: Date): JSONValue = JSONString(value.toString)

//  given userConverter: JSONConverter[User] with
//    override def convert(value: User): JSONObject = JSONObject(Map(
//      "name" -> JSONString(value.name),
//      "age" -> JSONNumber(value.age),
//      "email" -> JSONString(value.email)
//    )) // in this case we convert to a JSON Object bc we have multiple fields in this class

//  given postConverter: JSONConverter[Post] with
//    override def convert(value: Post): JSONObject = JSONObject(Map(
//      "content" -> JSONString(value.content),
//      "createdAt" -> JSONString(value.createdAt.toString),
//    ))

  // we can convert using already implemented converters, not to repeat code !!!

//  given userConverter: JSONConverter[User] with
//    override def convert(value: User): JSONObject = JSONObject(Map(
//      "name" -> stringConverter.convert(value.name),
//      "age" -> JSONNumber(value.age),
//      "email" -> stringConverter.convert(value.email)
//    )) // in this case we convert to a JSON Object bc we have multiple fields in this class
//
//  given postConverter: JSONConverter[Post] with
//    override def convert(value: Post): JSONObject = JSONObject(Map(
//      "content" -> stringConverter.convert(value.content),
//      "createdAt" -> stringConverter.convert(value.createdAt.toString),
//    ))
//
//  given feedConverter: JSONConverter[Feed] with
//    override def convert(feed: Feed): JSONObject = JSONObject(Map(
//      "user" -> userConverter.convert(feed.user),
//      "posts" -> JSONArray(feed.posts.map(post => postConverter.convert(post)))
//    ))

  // we can use directly the available converter for each data type (caution, for this, the apply method on the JSONConverter object must be USING an instance of JSONConverter)
  given userConverter: JSONConverter[User] with
    override def convert(value: User): JSONObject = JSONObject(Map(
      "name" -> JSONConverter[String].convert(value.name),
      "age" -> JSONConverter[Int].convert(value.age),
      "email" -> JSONConverter[String].convert(value.email)
    )) // in this case we convert to a JSON Object bc we have multiple fields in this class

  given postConverter: JSONConverter[Post] with
    override def convert(value: Post): JSONObject = JSONObject(Map(
      "content" -> JSONConverter[String].convert(value.content),
      "createdAt" -> JSONConverter[String].convert(value.createdAt.toString),
    ))

  given feedConverter: JSONConverter[Feed] with
    override def convert(feed: Feed): JSONObject = JSONObject(Map(
      "user" -> JSONConverter[User].convert(feed.user),
      "posts" -> JSONArray(feed.posts.map(post => JSONConverter[Post].convert(post)))
    ))

  // step 2.3 - user-facing API
  object JSONConverter {
    def convert[T](value: T)(using converter: JSONConverter[T]): JSONValue =
      converter.convert(value)

    def apply[T](using instance: JSONConverter[T]): JSONConverter[T] = instance
  }

  // example
  val now = new Date(System.currentTimeMillis())
  val john = User("John", 34, "john@rockthejvm.com")
  val feed = Feed(john, List(
    Post("Hello, I'm learning type classes", now),
    Post("Look at this cute puppy", now)
  ))

  // step 2.4 - extension methods
  object JSONSyntax {
    extension [T](value: T)
      def toIntermediate(using converter: JSONConverter[T]): JSONValue =
        converter.convert(value)
      def toJSON(using converter: JSONConverter[T]): String =
        toIntermediate.stringify
  }


  def main(args: Array[String]): Unit = {
    println(JSONConverter.convert(feed).stringify)
    import JSONSyntax.*
    println(feed.toIntermediate.stringify)
    println(feed.toJSON)
  }
}
