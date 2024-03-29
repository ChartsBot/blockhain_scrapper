package com.chartsbot.models

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.Web3Connector
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.exceptions.ClientConnectionException

import java.math.BigInteger
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

trait Web3DAO {

  def getLastBlockNumber(chain: SupportedChains): BigInteger

  def getBlock(number: Long, chain: SupportedChains): EthBlock.Block

  /**
    * Return a simple health check to see if we can execute a basic query against the given chain
    * @return true if everything is great, false otherwise.
    */
  def isAlive(chain: SupportedChains): Boolean

}

class DefaultWeb3DAO @Inject() (web3Connector: Web3Connector, implicit val ec: ExecutionContext)
  extends Web3DAO with LazyLogging {

  private def selectWeb3(chain: SupportedChains) = chain match {
    case com.chartsbot.models.SupportedChains.Polygon => web3Connector.web3jPolygonHttp
    case com.chartsbot.models.SupportedChains.Ethereum => web3Connector.web3jEthHttp
    case com.chartsbot.models.SupportedChains.Bsc => web3Connector.web3jBscHttp
    case com.chartsbot.models.SupportedChains.Ftm => web3Connector.web3jFtmHttp
  }

  def getLastBlockNumber(chain: SupportedChains): BigInteger = {
    val w3 = selectWeb3(chain)
    retry(0, chain, "getLastBlockNumber") {
      w3.ethBlockNumber().send().getBlockNumber
    }
  }

  def getBlock(blockNumber: Long, chain: SupportedChains): EthBlock.Block = {
    val w3 = selectWeb3(chain)
    retry(0, chain, "getBlock") {
      w3.ethGetBlockByNumber(new DefaultBlockParameterNumber(BigInteger.valueOf(blockNumber)), false).send().getBlock
    }
  }

  def isAlive(chain: SupportedChains): Boolean = {
    try {
      selectWeb3(chain).ethBlockNumber().send().getBlockNumber
      true
    } catch {
      case _: Throwable => false
    }
  }

  @tailrec
  private def retry[T](retryTime: Int, chain: SupportedChains, functionName: String)(f: => T): T = {
    try {
      f
    } catch {
      case e: ClientConnectionException =>
        if (retryTime > 5) {
          logger.error(s"${chain.toString} - Error on $functionName, tried too much and still getting error ${e.getMessage}.")
          throw new Exception(s"${chain.toString} - Error on $functionName, tried too much and still getting error.", e)
        } else {
          logger.warn(s"${chain.toString} - Client connection exception on $functionName, retry n°${retryTime + 1}. ${e.getMessage}")
          Thread.sleep(2500 * (retryTime + 1))
          retry(retryTime + 1, chain, functionName)(f)
        }
      case e: Throwable =>
        if (retryTime > 5) {
          logger.error(s"${chain.toString} - Error on $functionName, tried too much and still getting error ${e.getMessage}.")
          throw new Exception(s"${chain.toString} - Error on $functionName, tried too much and still getting error.", e)
        } else {
          logger.warn(s"${chain.toString} - Unknown exception on $functionName, retry n°${retryTime + 1}. ${e.toString}")
          Thread.sleep(2500 * (retryTime + 1))
          retry(retryTime + 1, chain, functionName)(f)
        }
    }
  }

}
