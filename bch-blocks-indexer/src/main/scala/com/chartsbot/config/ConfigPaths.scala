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
    final val WEB3_ETH_HTTP_URL = "web3.eth.connection.url"
    final val WEB3_ETH_WEBSOCKET_URL = "web3.eth.connection.websocketUrl"
    final val WEB3_POLYGON_HTTP_URL = "web3.polygon.connection.websocketUrl"
    final val WEB3_POLYGON_WEBSOCKET_URL = "web3.polygon.connection.websocketUrl"
  }

}