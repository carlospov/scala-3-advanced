package com.rockthejvm.part3async

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}


object Futures {

  def calculateMeaningOfLife(): Int = {
    //long computation
    Thread.sleep(1000)
    42
  }

  // thread pool (Java-Specific)
  val executor = Executors.newFixedThreadPool(4)
  // thread pool (Scala-specific)
  given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor) // given to make explicit the execution context injected into onComplete

  // a future = an async computation that will finish at some point
  // val aFuture = Future.apply(calculateMeaningOfLife())(executionContext)
  val aFuture = Future.apply(calculateMeaningOfLife()) // no need to specify execution context since it's given as explicit for all computations
  // worth knowing that the computation (in this case calculateMeaningOfLife()) is evaluated by name, so it will only be computed on the thread
  // where the future is evaluated


  // Option[Try[Int]]
  // - we don't know if we have a value
  // - if we do, it may be a failed computation
  val futureInstantResult: Option[Try[Int]] = aFuture.value

  // callbacks
  aFuture.onComplete {
    case Success(value) => println(s"I've competed with the meaning of life: $value")
    case Failure(ex) => println(s"Async computation failed: $ex")
  } // callbacks evaluated on SOME other thread, we can't know beforehand

  def main(args: Array[String]): Unit = {
    println(aFuture.value) // inspect the value of the future RIGHT NOW, that's why future.value is of type Option[Try[...]]
    Thread.sleep(1001)
    executor.shutdown() // to stop the application
    println(aFuture.value) // inspect the value of the future RIGHT NOW, that's why future.value is of type Option[Try[...]]

  }
}
