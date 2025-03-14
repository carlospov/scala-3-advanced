package com.rockthejvm.part5ts

import scala.util.Try

object HigherKindedTypes {

  // generic types where type args are generics
  class HigherKindedType[F[_]] // hkt
  class HigherKindedType_v2[F[_], G[_], A] // hkt with generics and normal types as args


  val hkexample = new HigherKindedType[List] // type constructor
  val hkexample2 = new HigherKindedType_v2[List, Option, String] // type constructor

  // can use hkts for methods aswell

  // why are them useful?
  // abstract libraries, e.g Cats
  // example: Functors
  val aList = List(1,2,3)
  val anOption = Option(2)
  val aTry = Try(42)

  val anIncrementedList = aList.map(_ + 1) // List(2,3,4)
  val anIncrementedOption = anOption.map(_ + 1) // Some(3)
  val anIncrementedTry = aTry.map(_ + 1) // Success 43

  // functor


  // "duplicated" APIs
  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def do10xTry(thetry: Try[Int]): Try[Int] = thetry.map(_ * 10)
  // we would like to not repeat ourselves, abstracting how map acts

  // DRY principle (Don't repeat yourself)
  // step 1: type class definition
  trait Functor[F[_]]  {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }
  // step 2: TC instances
  given listFunctor: Functor[List] with
    override def map[A, B](list: List[A])(f: A => B): List[B] = list.map(f)

  // step 3: offer a user-facing API
  def do10x[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ * 10)

  // if we create TC instances for Option and Try, the do10x method will work on Option and Try toi

  // step 4: extension methods
  extension [F[_], A](container: F[A])(using functor: Functor[F])
    def map[B](f: A => B): F[B] = functor.map(container)(f)

  def do10x_v2[F[_] : Functor](container: F[Int]): F[Int] =
    container.map(_ * 10) // map is an extension method

  /**
   * Exercise:
   * Implement a new type class on the same structure as Functor (extending it)
   * In the general API, must use for-comprehensions
   */

  def combineList[A, B](listA: List[A], listB: List[B]): List[(A,B)] =
    for {
      a <- listA
      b <- listB
    } yield (a,b)

  def combineOption[A, B](optionA: Option[A], optionB: Option[B]): Option[(A,B)] =
    for {
      a <- optionA
      b <- optionB
    } yield (a,b)

  def combineTry[A, B](tryA: Try[A], tryB: Try[B]): Try[(A,B)] =
    for {
      a <- tryA
      b <- tryB
    } yield (a,b)

  // step 1
  trait Magic[F[_]] extends Functor[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  // step 2: TC instances
  given listMagic: Magic[List] with
    override def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
    override def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] = fa.flatMap(f)

  // step 3: offer user-facing API
  def combine[F[_], A, B](fa: F[A], fb: F[B])(using magic: Magic[F]): F[(A,B)] =
    magic.flatMap(fa)(a => magic.map(fb)(b => (a,b)))

  // step 4: extension method
  extension [F[_], A](container: F[A])(using magic: Magic[F])
    def flatMap[B](f: A => F[B]): F[B] = magic.flatMap(container)(f)

  def combine_v2[F[_] : Magic, A, B](fa: F[A], fb: F[B]): F[(A,B)] =
    for {
      a <- fa
      b <- fb
    } yield (a, b)



  // Magic is in fact, Monad





  def main(args: Array[String]): Unit = {
    println(do10x(List(1,2,3)))
  }
}
