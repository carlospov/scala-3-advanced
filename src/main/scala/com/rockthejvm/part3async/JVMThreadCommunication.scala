package com.rockthejvm.part3async

import scala.collection.mutable
import scala.util.Random

object JVMThreadCommunication {

  def main(args: Array[String]): Unit = {
    ProdConsV3.start(10)
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

// larger container with queues
// producer -> [_ _ _ _] -> consumer
object ProdConsV3 {
  def start(containerCapacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    val consumer = new Thread(() => {
      val random = new Random(System.nanoTime())

      while (true) {
        buffer.synchronized {
          // thread-safe area
          if (buffer.isEmpty) {
            println("[consumer] buffer empty waiting...")
            buffer.wait()
          }

          // buffer not empty
          val x = buffer.dequeue()
          println(s"[consumer] I have consumed a value: $x")

          // "producer, give me more elements"
          buffer.notify() // wake up the producer if its asleep

        }

        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random(System.nanoTime())
      var counter = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == containerCapacity) {
            println(s"[producer] Buffer full, waiting")
            buffer.wait()
          }

          // buffer not full
          val newElement = counter
          counter +=1
          println(s"[producer] I am producing, the new value is $newElement")
          buffer.enqueue(newElement)

          // "consumer, don't be lazy!"
          buffer.notify() // this wakes up consumer if it's asleep
          // buffer.notifyAll()
        }

        Thread.sleep(random.nextInt(500))
      }

    })

    consumer.start()
    producer.start()
  }
}
