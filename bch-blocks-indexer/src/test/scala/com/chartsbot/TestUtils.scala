package com.chartsbot

import scala.util.Random

object TestUtils {

  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  def generateRandomString(n: Int = 10): String = (1 to n).map(_ => alpha(Random.nextInt(alpha.length))).mkString

}
