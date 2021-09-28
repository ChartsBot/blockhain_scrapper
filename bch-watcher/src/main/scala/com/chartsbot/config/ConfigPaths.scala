package com.chartsbot.config

object ConfigPaths {

  trait EcPaths {
    final val EC_NUMBER_THREADS = "ec.thread-number"
  }

  trait SqlPaths {
    final val MYSQL_CONNECTION_PATH = "sql.connection.path"
    final val MYSQL_CONNECTION_USERNAME = "sql.connection.username"
    final val MYSQL_CONNECTION_PASSWORD = "sql.connection.password"
  }

  trait Web3Paths {
    final val WEB3_HTTP_URL = "web3.connection.url"
    final val WEB3_WEBSOCKET_URL = "web3.connection.websocketUrl"
  }

}
