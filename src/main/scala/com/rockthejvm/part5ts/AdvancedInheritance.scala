package com.rockthejvm.part5ts

object AdvancedInheritance {

  // 1- composite types can be used on their own
  trait Writer[T] {
    def write(value: T): Unit
  }

  trait Stream[T] {
    def foreach(f: T => Unit): Unit
  }

  trait Closeable {
    def close(status: Int): Unit
  }

  // class MyDataStream extends Writer[String] with Stream[String] with Closeable {...}

  def processSteam[T](stream: Writer[String] with Stream[String] with Closeable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // 2 - diamond problem
  trait Animal { def name: String }

  trait Lion extends Animal {
    override def name: String = "Lion"
  }
  trait Tiger extends Animal {
    override def name: String = "Tiger"
  }
  class Liger extends Lion with Tiger // which name is used???
  class Liger_v2 extends Tiger with Lion // which name is used???

  def demoLiger(): Unit = {
    val liger = new Liger
    println(liger.name) // "Tiger"
    val liger_v2 = new Liger_v2
    println(liger_v2.name) //"Lion"
  } // because when both traits have the implementation of a same method, it occurs an override when extending
  // and the override is in the order in which is extended
  // last override perseveres


  // 3 - the super problem
  // what "super" means in Scala??
  trait Cold { //cold colors
    def print() = println("cold")
  }
  trait Green extends Cold {
    override def print(): Unit = {
      println("green")
      super.print()
    }
  }

  trait Blue extends Cold {
    override def print(): Unit = {
      println("blue")
      super.print()
    }
  }

  trait Red {
    def print() = println("red")
  }

  class White extends Red with Green with Blue {
    override def print(): Unit = {
      println("white")
        super.print()
    }
  }

  def demoColorInheritance(): Unit = {
    val white = White()
    white.print()
  }
// outputs
//  white
//  blue
//  green
//  cold

  // because what actually happens is that White extends ((Red -extended- with Green) -extended- with Blue)
  // so super is from that big parenthesis. Green overrides the print from Red, so 'red' is never shown. Now that is
  // the super class that gets extended with Blue. Hope that makes sense in the future...

  // daniel: "Type linearization" and... The above is correct

  def main(args: Array[String]): Unit = {
    demoLiger()
    demoColorInheritance()
  }
}
