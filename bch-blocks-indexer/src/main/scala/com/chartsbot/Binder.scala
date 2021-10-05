package com.chartsbot

import com.chartsbot.models.{ DefaultSqlBlocksDAO, DefaultWeb3DAO, SqlBlocksDAO, Web3DAO }
import com.chartsbot.services._
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.{ AbstractModule, Module }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Binder extends AbstractModule {

  def SqlClient: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[DefaultMySQLConnector])
  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def ExecutionContext: ScopedBindingBuilder = bind(classOf[ExecutionContext]).toProvider(classOf[ExecutionProvider])
  def SqlBlocksDao: ScopedBindingBuilder = bind(classOf[SqlBlocksDAO]).to(classOf[DefaultSqlBlocksDAO])
  def Web3Connector: ScopedBindingBuilder = bind(classOf[Web3Connector]).to(classOf[DefaultWeb3Connector])
  def BlockIndexerService: ScopedBindingBuilder = bind(classOf[BlockIndexerService]).to(classOf[DefaultBlockIndexerService])
  def Web3DAO: ScopedBindingBuilder = bind(classOf[Web3DAO]).to(classOf[DefaultWeb3DAO])

  override def configure(): Unit = {
    SqlClient
    Config
    ExecutionContext
    SqlBlocksDao
    Web3Connector
    BlockIndexerService
    Web3DAO
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
