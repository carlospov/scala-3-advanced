package com.rockthejvm.part3async

import java.util.concurrent.Executors

object JVMConcurrencyIntro {
  def BasicThreads(): Unit = {
    val runnable = new Runnable {
      override def run(): Unit = {
        println("waiting...")
        Thread.sleep(1000)
        println("Running on some thread")
      }
    }
    val aThread = new Thread(runnable)
    // threads on the JVM
    aThread.start() // will run the runnable on some jvm thread, the start call gives the signal
    // 1 JVM thread == 1 OS thread (soon to change via Project Loom, there'll be less OS threads than JVM threads)
    // block principal thread until the other thread finishes:
    aThread.join()
  }

  // order of execution is NOT guaranteed
  // in general: different runs == different results
  def orderOfExecution(): Unit = {
    val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
    val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("good bye")))

    threadHello.start()
    threadGoodbye.start()
  }

  // executors
  def demoExecutors(): Unit = {
    val threadPool = Executors.newFixedThreadPool(4)
    // submitting a computation
    threadPool.execute(() => println("smth in the threadPool"))

    // we can execute as much runnables as we want, main thread where the jvm is running doesn't stop processing
    // it will keep scheduling runnables to threads as these are running
    threadPool.execute(() =>
      Thread.sleep(1000)
      println("done after one second")
    )

    threadPool.execute(() =>
      Thread.sleep(1000)
      println("almost done")
      Thread.sleep(1000)
      println("done after 2 secs")
    )

    threadPool.shutdown()
    // threadPool.execute(() => println("this should NOT appear")) // in fact, it throws an exception
  }



  def main(args: Array[String]): Unit = {
    // BasicThreads()
    // orderOfExecution()
    demoExecutors() // this executes the threadpool, the threadpool manages the lifecycle of the threads inside it but
    // not its own, so when we run something on the threadpool, and it finishes, threadpool stays up. ThreadPool.shutdown() to shutdown
  }
}
