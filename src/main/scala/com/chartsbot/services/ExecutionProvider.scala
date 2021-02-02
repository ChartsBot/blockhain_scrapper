package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.EcPaths
import com.typesafe.config.Config

import java.util.concurrent.Executors
import javax.inject.{ Inject, Provider, Singleton }
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

@Singleton
class ExecutionProvider @Inject() (config: Config) extends Provider[ExecutionContext] with EcPaths {

  val threadPoolSize: Int = config.getInt(EC_NUMBER_THREADS)

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadPoolSize))

  override def get(): ExecutionContext = ec

}
