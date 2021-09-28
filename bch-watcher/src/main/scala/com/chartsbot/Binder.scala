package com.chartsbot

import com.chartsbot.services.{ BlockchainWatcherService, ConfigProvider, DefaultBlockchainWatcherService, DefaultMySQLConnector, DefaultWeb3Connector, ExecutionProvider, MySQLConnector, Web3Connector }
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.{ AbstractModule, Module }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Binder extends AbstractModule {

  def SqlClient: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[DefaultMySQLConnector])
  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def ExecutionContext: ScopedBindingBuilder = bind(classOf[ExecutionContext]).toProvider(classOf[ExecutionProvider])
  def Web3Connector: ScopedBindingBuilder = bind(classOf[Web3Connector]).to(classOf[DefaultWeb3Connector])
  def BlockchainWatcherService: ScopedBindingBuilder = bind(classOf[BlockchainWatcherService]).to(classOf[DefaultBlockchainWatcherService])

  override def configure(): Unit = {
    SqlClient
    Config
    ExecutionContext
    Web3Connector
    BlockchainWatcherService
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
