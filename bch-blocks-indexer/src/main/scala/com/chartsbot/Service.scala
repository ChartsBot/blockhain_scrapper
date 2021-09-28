package com.chartsbot

import com.chartsbot.services.BlockIndexerService

import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    for ((k, v) <- environmentVars) println(s"envVar: $k, value: $v")

    get[BlockIndexerService].run()
  }

}
