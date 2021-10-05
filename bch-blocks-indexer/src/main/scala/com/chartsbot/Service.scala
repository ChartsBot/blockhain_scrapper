package com.chartsbot

import com.chartsbot.services.BlockIndexerService

import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    val r = for ((k, v) <- environmentVars) yield s"envVar: $k, value: $v"
    logger.info(r.toList.mkString(", "))

    get[BlockIndexerService].run()
  }

}
