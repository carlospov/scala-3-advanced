package com.rockthejvm.part4context

import scala.concurrent.{ExecutionContext, Future}

object ContextFunctions {

  //
  val aList = List(2,3,1,4)
  val sortedList = aList.sorted

  // defs can take using clauses
  def methodWithoutContextArguments(nonContextArg: Int)(nonContextArg2: String): String = ???
  def methodWithContextArguments(nonContextArg: Int)(using nonContextArg2: String): String = ???

  // we can convert methods to function values via eta-expansion
  val functionWithoutContextArguments = methodWithoutContextArguments
  //val func2 = methodWithContextArguments // the compiler can't know how to turn this method to a function value because of the context argument

  // context function: it can take contextual parameters
  val functionWithContextArguments: Int => String ?=> String = methodWithContextArguments  // now it works, we have to specify the output type signature and place a "?" on the argument being passed as using clause

  // defined this function, we can call it naturally
  val someResult = functionWithContextArguments(2)(using "Scala")

  /* uses for this:
  - convert methods with using clauses to function values
  - HOF with function values taking given instances as arguments
  - Requiring execution context at CALL SITE not at DEFINITION SITE

   */
  // execution context here
  //val incrementAsync: Int => Future[Int] = x => Future(x + 1) // to do this we need an execution context where to run, but execution context is taken from where it's defined
  // this is annoying, because we would like execution context to be taken from where the code is USED/CALLED
  // in order to decouple that we can use context functions

  val incrementAsync: ExecutionContext ?=> Int => Future[Int] = x => Future(x + 1)

  def main(args: Array[String]): Unit = {

  }
}
