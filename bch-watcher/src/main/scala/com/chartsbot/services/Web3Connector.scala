package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.Web3Paths
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

import javax.inject.{ Inject, Singleton }

trait Web3Connector {
  val web3: Web3j
}

@Singleton
class DefaultWeb3Connector @Inject() (config: Config) extends Web3Connector with Web3Paths with LazyLogging {

  val urlWeb3Provider: String = config.getString(WEB3_WEBSOCKET_URL)

  val web3jService = new WebSocketService(urlWeb3Provider, false)
  web3jService.connect()

  val web3: Web3j = Web3j.build(web3jService)

}
