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
    final val WEB3_POLYGON_HTTP_URL = "web3.polygon.connection.url"
    final val WEB3_POLYGON_WEBSOCKET_URL = "web3.polygon.connection.websocketUrl"
    final val WEB3_BSC_HTTP_URL = "web3.bsc.connection.url"
    final val WEB3_BSC_WEBSOCKET_URL = "web3.bsc.connection.websocketUrl"
    final val WEB3_FTM_HTTP_URL = "web3.ftm.connection.url"
    final val WEB3_FTM_WEBSOCKET_URL = "web3.ftm.connection.websocketUrl"
  }

  trait ScalatraPaths {
    final val SCALATRA_ENV = "scalatra.env"
  }

  trait ServerPaths {
    final val SERVER_PORT = "scalatra.port"
    final val SERVER_BASE_URL = "scalatra.base_url"
    final val APP_VERSION = "scalatra.version"
  }

}
