package com.chartsbot

import com.chartsbot.services.{ BlockchainWatcherService }

import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    for ((k, v) <- environmentVars) println(s"envVar: $k, value: $v")

    get[BlockchainWatcherService].run()
  }

}
