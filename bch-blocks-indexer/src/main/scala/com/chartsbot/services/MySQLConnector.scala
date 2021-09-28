package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.SqlPaths
import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill._

import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._
import java.net.URI

trait MySQLConnector {

  val ctx: MysqlAsyncContext[CamelCase.type]

}

@Singleton
class DefaultMySQLConnector @Inject() (config: Config) extends MySQLConnector with SqlPaths {

  println(config.getString(MYSQL_CONNECTION_PATH))
  println(config.getString(MYSQL_CONNECTION_PASSWORD))

  val uri = new URI(config.getString(MYSQL_CONNECTION_PATH))

  println(uri.getScheme)

  val cfg: Config = ConfigFactory.parseMap(Map(
    "dataSourceClassName" -> "dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource",
    "url" -> config.getString(MYSQL_CONNECTION_PATH),
    "user" -> config.getString(MYSQL_CONNECTION_USERNAME),
    "password" -> config.getString(MYSQL_CONNECTION_PASSWORD),
    "cachePrepStmts" -> "true",
    "prepStmtCacheSize" -> 250,
    "prepStmtCacheSqlLimit" -> 2048,
    "connectionTimeout" -> 30000
  ).asJava)

  val conf: MysqlAsyncContextConfig = MysqlAsyncContextConfig(cfg)

  lazy val ctx: MysqlAsyncContext[CamelCase.type] = new MysqlAsyncContext(CamelCase, conf)

}
