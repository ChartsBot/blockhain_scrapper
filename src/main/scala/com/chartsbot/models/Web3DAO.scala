package com.chartsbot.models

import com.chartsbot.services.Web3Connector
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.response.{EthBlock, EthBlockNumber}

import javax.inject.{Inject, Singleton}
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}

trait Web3DAO {

  def getBlock(blockNumber: Long): Future[EthBlock]

  def getLastBlock: Future[Long]

}

@Singleton
class DefaultWeb3DAO @Inject () (web3Connector: Web3Connector)(implicit val ec: ExecutionContext) extends Web3DAO {

  val web3Client: Web3j = web3Connector.web3

  def getBlock(blockNumber: Long): Future[EthBlock] = {
    val blockParameterNumber = new DefaultBlockParameterNumber(blockNumber)
    web3Client.ethGetBlockByNumber(blockParameterNumber, false).sendAsync().toScala
  }

  def getLastBlock: Future[Long] = web3Client.ethBlockNumber().sendAsync().toScala.map(_.getBlockNumber.longValue())
}
