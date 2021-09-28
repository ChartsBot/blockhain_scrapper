package com.chartsbot.services

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.reactivex.functions.Consumer
import io.reactivex.{ Flowable, Observable }
import org.web3j.abi.datatypes.Event
import org.web3j.contracts.token.ERC20Interface
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.{ EthBlock, Transaction }

import java.math.BigInteger
import javax.inject.{ Inject, Singleton }
import scala.::
import scala.concurrent.Await
import scala.collection.JavaConverters._

trait BlockchainWatcherService {

  def run(): Unit

}

@Singleton
class DefaultBlockchainWatcherService @Inject() (web3Connector: Web3Connector) extends BlockchainWatcherService with LazyLogging {

  val web3 = web3Connector.web3

  def run() = {

    val startingBlock = DefaultBlockParameter.valueOf(BigInteger.valueOf(11796347))

    val a: Flowable[EthBlock] = web3.replayPastAndFutureBlocksFlowable(startingBlock, true)

    val subscription = a.subscribe(new BlockConsumer)

    val event = new Event("Transfert", "")

    ERC20Interface.

      logger.info("I'm here")

  }

}

class BlockConsumer extends Consumer[EthBlock] with LazyLogging {

  override def accept(block: EthBlock): Unit = {
    val blockNum = block.getBlock.getNumber.intValue().toString
    val txs = block.getBlock.getTransactions.asScala.map(_.get().asInstanceOf[Transaction])
    logger.info("there are " + txs.length + " txs in the block " + blockNum)
    for (tx <- txs) {
      if (tx.getHash.toLowerCase == "0x75df5cd49d57061e4f0a49f2a9938cbc16801a38aaa0cc5fc65ac682b12ab523".toLowerCase) {
        logger.info("FOUND")
        println(tx.getRaw)
        println(tx.getValueRaw)
        println(tx.getInput)

      }

    }
  }
}
