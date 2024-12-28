package com.rockthejvm.part3async

object JVMConcurrencyProblems {

  def runInParallel(): Unit = {
    var x = 0

    val thread1 = new Thread (() => {
      x = 1
    })

    val thread2 = new Thread (() => {
      x = 2
    })

    thread1.start()
    thread2.start()

    println(x)  // race condition, eventually thread1 will finish after thread2 even when thread2 is called later
  }


  // synchronization

  case class BankAccount(var amount: Int)

  def buy(account: BankAccount, thing: String, price: Int): Unit = {
    account.amount -= price // 3 steps: read, compute and write
  }

  /*
  Example race condition:
  thread1 (shoes)
    reads amount to be 50.000 USD
    compute 50.000 - 3.000 = 47.000
  thread2 (iphone)
    reads amount to be 50.000 USD
    computes 50.000 - 4.000 = 46.000
  thread1 (shoes)
    write amount to 47.000
  thread2 (iphone)
    write amount to 46.000

  final result => amount = 46.000
   */

  // to solve this, we have to define an ATOMIC buy method that can't cause the race problem
  def buySafe(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    bankAccount.synchronized {  // synchronized does not allow multiple threads to run the critical section AT THE SAME TIME
      bankAccount.amount -= price // critical section
    }
  }

  def demoBankingProblem(): Unit = {
    (1 to 10000).foreach { _ =>
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "iphone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43000) println(s"NS - Bank broken, amount: ${account.amount}") // it can happen sometimes (it did)
    }
  }
  def demoSafeBankingProblem(): Unit = {
    (1 to 10000).foreach { _ =>
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buySafe(account, "shoes", 3000))
      val thread2 = new Thread(() => buySafe(account, "iphone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43000) println(s"S - Bank broken, amount: ${account.amount}") // it can happen sometimes (it did)
    }
  }


  /**
   * Exercise
   * 1 - Create "inception threads"
   *    thread 1
   *        -> thread 2
   *            -> thread 3
   *                -> ...
   *    each thread prints "hello from thread $i" and in reverse order
   *
   */

  def inceptionThreads(n: Int, i: Int): Unit = {
    if (n > 0)
      val subthread = new Thread( () => { // if there's still n left, create another subthread and print in which thread you are
        inceptionThreads(n - 1, i + 1)
        println(s"Hello from thread $i")
      })
      subthread.start() // start/run the thread that we just created
      subthread.join() // "end" that same thread
    else ()
  }

  // this way, this will happen: let n=4
  /*
  thread1
    -> thread 2
      -> thread 3
        -> thread 4
          - it will try to create another thread, but n will be 0
          - print 4
        - print 3
      - print 2
    - print 1
   */

  // this solution works but, solution from daniel is:
  def inceptionThreads_daniel(n: Int, i: Int): Thread = {
    new Thread(() => {
      if (i < n) {
        val newThread = inceptionThreads_daniel(n, i + 1)
        newThread.start()
        newThread.join()
      }
      println(s"Hello from thread $i")
    })
  }



  /**
   * Exercise 2:
   *  What's the max/min value of x
    */

  def minMaxX(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }

  // the min value is 1 in the edge case all threads reading occur at same time
  // the max value is 100 in the other case, the one in which all reads occur one after another

  /**
   * Exercise 3:
   * "sleep fallacy"
   * - What's the value of message?
   */

  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome"
    })

    message = "Scala sucks"
    awesomeThread.start()
    Thread.sleep(1001)
    awesomeThread.join() // <-- solution
    println(message)
  }

  // unknown, it depends on how instructions are sent to physical cores, memory speed, OS
  /* daniel's explanation
  main thread
    message = "Scala sucks"
    awesomeThread.start()
    sleep(1001) -- in some builds(os + jvm + hardware) sleep yields execution, that means it will schedule other thread for execution
                   for example, imagine one single core processor, it will sleep the thread by 1001 ms while running a 1000 ms process on another thread, both prints
                  will be postponed at least 1 second. Imagine that during the 1 seconds interval the OS gives the cpu to another thread for running a priority task
                  that ends in 2 seconds. Then both sleeps would have finished and can happen that the task after the 1001ms sleep is given to the cpu, printing "scala sucks"
    The solution is to join the worker thread
   */

  def main(args: Array[String]): Unit = {
    // runInParallel()
    //demoBankingProblem()
    //demoSafeBankingProblem()
    //inceptionThreads(4, 1)
    demoSleepFallacy()
  }
}
