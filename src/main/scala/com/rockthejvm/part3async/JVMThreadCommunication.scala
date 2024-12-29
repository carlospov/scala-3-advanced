package com.rockthejvm.part3async

import scala.collection.mutable
import scala.util.Random

object JVMThreadCommunication {

  def main(args: Array[String]): Unit = {
    ProdConsV4.start(3, 1, 5)
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

// large container + multiple consumers and producers
// producer1, producer 2, ... -> [_ _ _ _] -> consumer1, consumer2, ...
object ProdConsV4 {

  class Consumer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random(System.nanoTime())

      while (true) {
        buffer.synchronized {
          // critical section
          while (buffer.isEmpty) { // it must be a while, because it can happen the following scenario:
            // producer 1 produces and notifies, consumer2 awakens and consumes, then notifies consumer1, that awakens and tries
            // to consume from an empty queue, which crashed the program. So... we must constantly check if buffer is empty
            println(s"[consumer $id] buffer empty, waiting...")
            buffer.wait()
          }

          // buffer not empty
          val newValue = buffer.dequeue()
          println(s"[consumer $id] consumed $newValue")

          // notify a producer
          /*
          Scenario: 2 producers, 1 consumer, capacity 1
          producer1 produces, then waits
          producer2 gets scheduled next and checks if buffer full and waits
          both producers waiting, the next thread awakened is consumer1
          consumer1 wakes up, consumes and notifies producer1
          consumer sees buffer empty and waits
          producer1 is scheduled, produces a value and notifies. This can awaken producer2 or consumer1 (wo don't really know)
          let the signal go to producer2, producer1 sees buffer full and waits, and producer2 sees buffer full and waits
          This is a DEADLOCK, every producer and every consumer is blocked and will never be awakened again !!!
           */
          buffer.notifyAll() // to wake up every thread
        }

        Thread.sleep(random.nextInt(500))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random(System.nanoTime())
      var currentCount = 0

      while (true) {
        buffer.synchronized {
          // critical section
          while (buffer.size == capacity) { // buffer full // same problem as before, it has to be a 'while', not an 'if'
            println(s"[producer $id] buffer full, waiting...")
            buffer.wait()
          }

          // buffer not full
          println(s"[producer $id] producing $currentCount")
          buffer.enqueue(currentCount)

          // wake up a consumer
          buffer.notifyAll()

          currentCount += 1
        }

        Thread.sleep(random.nextInt(500))
      }
    }
  }

  def start(nProducers: Int, nConsumers: Int, containerCapacity: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]

    val producers = (1 to nConsumers).map(id => new Producer(id, buffer, containerCapacity))
    val consumers = (1 to nProducers).map(id => new Consumer(id, buffer, containerCapacity))

    producers.foreach(_.start())
    consumers.foreach(_.start())
  }
}
