package com.chartsbot

import com.chartsbot.models.{ DefaultEthSqlBlocksDAO, DefaultPolygonSqlBlocksDAO, SqlBlocksEthDAO, SqlBlocksPolygonDAO }
import com.chartsbot.services._
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.{ AbstractModule, Module }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Binder extends AbstractModule {

  def SqlClient: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[DefaultMySQLConnector])
  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def ExecutionContext: ScopedBindingBuilder = bind(classOf[ExecutionContext]).toProvider(classOf[ExecutionProvider])
  def SqlFilesEthDao: ScopedBindingBuilder = bind(classOf[SqlBlocksEthDAO]).to(classOf[DefaultEthSqlBlocksDAO])
  def SqlFilesPolygonDao: ScopedBindingBuilder = bind(classOf[SqlBlocksPolygonDAO]).to(classOf[DefaultPolygonSqlBlocksDAO])
  def Web3Connector: ScopedBindingBuilder = bind(classOf[Web3Connector]).to(classOf[DefaultWeb3Connector])
  def BlockIndexerService: ScopedBindingBuilder = bind(classOf[BlockIndexerService]).to(classOf[DefaultBlockIndexerService])

  override def configure(): Unit = {
    SqlClient
    Config
    ExecutionContext
    SqlFilesEthDao
    SqlFilesPolygonDao
    Web3Connector
    BlockIndexerService
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
