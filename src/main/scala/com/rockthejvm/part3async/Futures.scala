package com.rockthejvm.part3async

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.{Failure, Random, Success, Try}


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
//  aFuture.onComplete {
//    case Success(value) => println(s"I've competed with the meaning of life: $value")
//    case Failure(ex) => println(s"Async computation failed: $ex")
//  } // callbacks evaluated on SOME other thread, we can't know beforehand


  /*
  Functional composition
   */
  case class Profile(id: String, name: String) {
    def sendMessage(anotherProfile: Profile, message: String) =
      println(s"${this.name} sending message to ${anotherProfile.name}: $message")
  }

  object SocialNetwork {
      // "database"
      val names = Map(
        "rtjvm.id.1-daniel" -> "Daniel",
        "rtjvm.id.2-jane" -> "Jane",
        "rtjvm.id.3-mark" -> "Mark",
      )

      val friends = Map (
        "rtjvm.id.2-jane" -> "rtjvm.id.3-mark"
      )

      val random = new Random()

      // "API"
      def fetchProfile(id: String): Future[Profile] = Future {
        // fetch something from database
        Thread.sleep(random.nextInt(500))
        Profile(id, names(id))
      }

      def fetchBestFriend(profile: Profile): Future[Profile] = Future {
        Thread.sleep(random.nextInt(400))
        val bestFriendId = friends(profile.id)
        Profile(bestFriendId, names(bestFriendId))
      }
   }

  // problem: sending a message to my best friend
  def sendMessageToBestFriend(accountID: String, message: String): Unit = {
    // 1- call fetchProfile
    // 2- cal fetchBestFriend
    // 3- call profile.sendMessage(bestFriend)
    val profileFuture = SocialNetwork.fetchProfile(accountID)
    profileFuture.onComplete {
      case Success(profile) => // "code block" between arrow and the next case
        val friendProfileFuture = SocialNetwork.fetchBestFriend(profile)
        friendProfileFuture.onComplete {
          case Success(friendProfile) => profile.sendMessage(friendProfile, message)
          case Failure(e) => e.printStackTrace()
        }
      case Failure(ex) => ex.printStackTrace()
    }
  } // this composes two futures, and even with two futures code looks difficult to read
  // onComplete is a hassle.
  // solution: functional composition



  def sendMessageToBestFriend_v2(accountID: String, message: String): Unit = {
    val profileFuture = SocialNetwork.fetchProfile(accountID)
    profileFuture.flatMap { profile => // Future[Unit]
      SocialNetwork.fetchBestFriend(profile).map { bestfriend => // Future[Unit]
        profile.sendMessage(bestfriend, message) // Unit
      }
    }
  }

  def sendMessageToBestFriend_v3(accountID: String, message: String): Unit = {
    for {
      profile <- SocialNetwork.fetchProfile(accountID)
      bestFriend <- SocialNetwork.fetchBestFriend(profile)
    } yield profile.sendMessage(bestFriend, message) // identical to version 2, but easier to read
  }


  val janeProfileFuture = SocialNetwork.fetchProfile("rtjvm.id.2-jane")
  val janeFuture: Future[String] = janeProfileFuture.map(profile => profile.name) // map transforms value contained inside ASYNCHRONOUSLY
  val janesBestFriend: Future[Profile] = janeProfileFuture.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val janesBestFriendFilter: Future[Profile] = janesBestFriend.filter(profile => profile.name.startsWith("Z"))

  // fallbacks (in case there's an exception)
  val profileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("rtjvm.id.0-dummy", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("rtjvm.id.0-dummy") // if first failes, this will be return, even when the second future is a failure
  } // so the exception returned will be the one emerging second future

  // fallback to
  val fallBackProfile: Future[Profile] = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("rtjvm.id.0-dummy")) // this case is different to the one before
  // if the first future is failed and the second too, the exception will be returned from the FIRST future


  /*
  Block for a future: Do it with caution, just when it's absolutely necessary
   */
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    // "APIs"
    def fetchUser(name: String): Future[User] = Future {
      // simulate db fetching
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate payment
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    // "external API"
    def purchase(username: String, item: String, merchantName: String, price: Double):  String = {
      /*
      1. fetch user
      2. create tx
      3. WAIT for the tx to finish
       */
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, price)
      } yield transaction.status

      // blocking call
      Await.result(transactionStatusFuture, 2.seconds) // throws kind of a timeout exception if transactionStatusFuture is not available within 2 seconds or future fails
      // the method .seconds for int is a non-native extension of Int type
    }
  }

  def main(args: Array[String]): Unit = {
//      println(aFuture.value) // inspect the value of the future RIGHT NOW, that's why future.value is of type Option[Try[...]]
//      Thread.sleep(1001)
//      executor.shutdown() // to stop the application
//      println(aFuture.value) // inspect the value of the future RIGHT NOW, that's why future.value is of type Option[Try[...]]
//    sendMessageToBestFriend_v3("rtjvm.id.2-jane", "Hey best friend")
    println("purchasing")
    println(BankingApp.purchase("daniel-234", "shoes", "merchant-798", 3.567))
    println("purchase complete")
    Thread.sleep(1001)
    executor.shutdown()
  }
}

