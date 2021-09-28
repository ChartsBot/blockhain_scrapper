package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.Web3Paths
import com.chartsbot.util.CustomWebSocketClient
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

import java.net.URI
import javax.inject.{ Inject, Singleton }

trait Web3Connector {
  val web3Eth: Web3j
  val web3Polygon: Web3j
}

@Singleton
class DefaultWeb3Connector @Inject() (config: Config) extends Web3Connector with Web3Paths with LazyLogging {

  val urlWeb3EthProvider: String = config.getString(WEB3_ETH_WEBSOCKET_URL)
  val urlWeb3PolygonProvider: String = config.getString(WEB3_POLYGON_WEBSOCKET_URL)

  val webSocketClientEth = new CustomWebSocketClient(new URI(urlWeb3EthProvider))
  val webSocketClientPolygon = new CustomWebSocketClient(new URI(urlWeb3PolygonProvider))

  val web3jEth = new WebSocketService(webSocketClientEth, false)
  web3jEth.connect()

  val web3jPolygon = new WebSocketService(webSocketClientPolygon, false)
  web3jPolygon.connect()

  val web3Eth: Web3j = Web3j.build(web3jEth)
  val web3Polygon: Web3j = Web3j.build(web3jPolygon)

}
