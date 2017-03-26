package com.nekopiano.scala.sandbox.shapeless

/**
  * Created on 02/02/2017.
  */
object ConvertingTest extends App {


  {
    val list = List("a", 1, 'b)
    val numList = List(1, 2, 3)

    import shapeless._
    import HList._
    import syntax.std.tuple._
    import syntax.std.traversable._

    val hlist = list.toSizedHList(3)
    //val hlist = list.toHList[Int]
    println(hlist)
  }

  {

    import shapeless._
    import syntax.std.traversable._

    val x = List(1, 2, 3)
    val xHList = x.toHList[Int :: Int :: Int :: HNil]
    val t = xHList.get.tupled
    println(t)
  }

  {
    val x = List(1, 2, 3)
    val y = List("a", 1, 'b)

    val t = x match {
      case List(a, b, c) => (a, b, c)
    }
    println(t)

    val u = y match {
      case List(a, b, c) => (a, b, c)
    }
    println(u)

  }

}
