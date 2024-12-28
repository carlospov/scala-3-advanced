package com.rockthejvm.part3async

object JVMThreadCommunication {

  def main(args: Array[String]): Unit = {
    ProdConsV2.start()
  }
}

// problem: The Producer-Consumer problem

class SimpleContainer {
  private var value: Int = 0

  def isEmpty: Boolean = value == 0

  def set(newValue: Int): Unit =
    value = newValue

  def get: Int = {
    val result = value
    value = 0
    result
  }
}

// P-C part 1: one producer and one consumer
object ProdConsV1 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      //busy waiting --> is very bad, it blocks the processor while the thread is alive
      while (container.isEmpty) {
        println("[consumer] waiting for a value...")
      }

      println(s"[consumer] I have consumed a value: ${container.get}")
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(1000)
      val value = 42
      println(s"[producer] I am producing, after a long work, the value is $value")
      container.set(value)
    })

    consumer.start()
    producer.start()
  }
}

// better way of doing this...
// with passive waiting: wait + notify
object ProdConsV2 {
  def start(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")

      container.synchronized { // block all other threads trying to "lock" this object
        // thread-safe code
        if (container.isEmpty) // (FIX 1)
          container.wait() // release the lock and suspend the thread (the thread is now dormant)
        // reacquire the lock
        // continue execution
        println(s"[consumer] I have consumed a value: ${container.get}")
      }

    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(1000)
      val value = 42

      //
      container.synchronized {
        println(s"[producer] I am producing, after a long work, the value is $value")
        container.set(value)
        container.notify() // awakes one suspended thread on this object, on our case it will be the only one, the consumer one
        // if there were more, we don't really know what thread is going to be awakened
      }
    })

    consumer.start() // WARNING!!! stating this first is no guarantee that consumer starts first, we have no control over how
    // the jvm + OS schedule threads
    // in the case the producer starts and finishes before the consumer even starts, then it will force the consumer to wait forever (FIX 1)
    producer.start()
  }
}
