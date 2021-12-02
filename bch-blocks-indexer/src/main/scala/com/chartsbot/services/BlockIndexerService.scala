package com.chartsbot.services

import com.chartsbot.models.SupportedChains.{ Bsc, Ethereum, Polygon, SupportedChains }
import com.chartsbot.models.{ SqlBlock, SqlBlocksDAO, SupportedChains, Web3DAO }
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.response.EthBlock

import java.math.BigInteger
import javax.inject.{ Inject, Singleton }
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.implicitConversions

trait BlockIndexerService {

  def run(): Unit

}

@Singleton
class DefaultBlockIndexerService @Inject() (web3Connector: Web3Connector, sqlBlocksDAO: SqlBlocksDAO, web3DAO: Web3DAO)(implicit val ec: ExecutionContext) extends BlockIndexerService with LazyLogging {

  def getLastIndexedBlock(chain: SupportedChains): Long = Await.result(sqlBlocksDAO.getLastBlock(chain), 10.seconds) match {
    case Left(value) =>
      logger.error(s"Error getting last indexed ${chain.toString} block: ", value)
      throw new Exception(s"Not good getting last indexed block ${chain.toString}")
    case Right(value) => value.head.number
  }

  val initBlockNumberPolygon: Long = getLastIndexedBlock(Polygon)

  val initBlockNumberEth: Long = getLastIndexedBlock(Ethereum)

  val initBlockNumberBsc: Long = getLastIndexedBlock(Bsc)

  def run(): Unit = {

    val goFastBlockNumberEthUntil = 13310000
    val goFastBlockNumberPolygonUntil = 19600000
    val goFastBlockNumberBscUntil = 11461538

    var t0 = System.currentTimeMillis()
    if (initBlockNumberPolygon < goFastBlockNumberPolygonUntil) {

      (initBlockNumberPolygon to goFastBlockNumberPolygonUntil).filter(_ % 50 == 0).foreach { i =>

        val block: EthBlock = try {
          web3Connector.web3jPolygonHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        } catch {
          case e: Exception =>
            logger.error("Catched exception: ", e)
            Thread.sleep(2000)
            logger.info("back on tracks")
            web3Connector.web3jPolygonHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        }

        val sqlBlock = SqlBlock(i.intValue(), block.getBlock.getTimestamp.intValue())
        val r = sqlBlocksDAO.addBlock(sqlBlock)(Polygon)
        Await.result(r, 10.seconds)

        if (i % 1000 == 0) {
          val t1 = t0
          t0 = System.currentTimeMillis()
          val time1000BlocksMs = t0 - t1
          val remainingTime = ((goFastBlockNumberPolygonUntil - i) / 1000) * (time1000BlocksMs / 1000)
          val rTimeHour = remainingTime.toFloat / 3600.toFloat
          logger.info(s"Polygon: processed block $i. Processed 1000 blocks in ${time1000BlocksMs}ms. Remaining time: ${remainingTime}s = ${rTimeHour}h")
        }

      }

    }

    if (initBlockNumberBsc < goFastBlockNumberBscUntil) {

      (initBlockNumberBsc to goFastBlockNumberBscUntil).filter(_ % 100 == 0).foreach { i =>

        val block: EthBlock = try {
          web3Connector.web3jBscHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        } catch {
          case e: Exception =>
            logger.error("Catched exception: ", e)
            Thread.sleep(2000)
            logger.info("back on tracks")
            web3Connector.web3jBscHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        }

        val sqlBlock = SqlBlock(i.intValue(), block.getBlock.getTimestamp.intValue())
        val r = sqlBlocksDAO.addBlock(sqlBlock)(Bsc)
        Await.result(r, 10.seconds)

        if (i % 1000 == 0) {
          val t1 = t0
          t0 = System.currentTimeMillis()
          val time1000BlocksMs = t0 - t1
          val remainingTime = ((goFastBlockNumberBscUntil - i) / 1000) * (time1000BlocksMs / 1000)
          val rTimeHour = remainingTime.toFloat / 3600.toFloat
          logger.info(s"Bsc: processed block $i. Processed 1000 blocks in ${time1000BlocksMs}ms. Remaining time: ${remainingTime}s = ${rTimeHour}h")
        }

      }

    }

    if (initBlockNumberEth < goFastBlockNumberEthUntil) {
      (initBlockNumberEth to goFastBlockNumberEthUntil).filter(_ % 50 == 0).foreach { i =>

        val block: EthBlock = try {
          web3Connector.web3jEthHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        } catch {
          case e: Exception =>
            logger.error("Catched exception: ", e)
            Thread.sleep(2000)
            logger.info("Back on tracks")
            web3Connector.web3jEthHttp.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()
        }

        val sqlBlock = SqlBlock(i.intValue(), block.getBlock.getTimestamp.intValue())
        val r = sqlBlocksDAO.addBlock(sqlBlock)(Ethereum)
        Await.result(r, 10.seconds)

        if (i % 1000 == 0) {
          val t1 = t0
          t0 = System.currentTimeMillis()
          val time1000BlocksMs = t0 - t1
          val remainingTime = ((goFastBlockNumberEthUntil - i) / 1000) * (time1000BlocksMs / 1000)
          val rTimeHour = remainingTime.toFloat / 3600.toFloat
          logger.info(s"Eth: processed block $i. Processed 1000 blocks in ${time1000BlocksMs}ms. Remaining time: ${remainingTime}s = ${rTimeHour}h")
        }

      }
    }

    var lastBlockIndexedPolygon = getLastIndexedBlock(Polygon)
    var lastBlockIndexedEth = getLastIndexedBlock(Ethereum)
    var lastBlockIndexedBsc = getLastIndexedBlock(Bsc)

    //
    //    web3Connector.web3Polygon.replayPastAndFutureBlocksFlowable(new DefaultBlockParameterNumber(lastBlockIndexedPolygon), false).subscribe(
    //      (e: EthBlock) => {
    //        indexBlockOfChainInReplay(e, 5, SupportedChains.Polygon)
    //      },
    //      (e: Throwable) => {
    //        logger.error("error handling stuff polygon", e)
    //      }
    //    )
    //
    //    logger.info("started poly")
    //
    //    web3Connector.web3Bsc.replayPastAndFutureBlocksFlowable(new DefaultBlockParameterNumber(lastBlockIndexedBsc), false).subscribe(
    //      (e: EthBlock) => {
    //        indexBlockOfChainInReplay(e, 5, SupportedChains.Bsc)
    //      },
    //      (e: Throwable) => {
    //        logger.error("error handling stuff bsc", e)
    //      }
    //    )
    //
    //    logger.info("started bsc")
    //
    //    web3Connector.web3Eth.replayPastAndFutureBlocksFlowable(new DefaultBlockParameterNumber(lastBlockIndexedEth), false).subscribe(
    //      (e: EthBlock) => {
    //        indexBlockOfChainInReplay(e, 1, SupportedChains.Ethereum)
    //      },
    //      (e: Throwable) => {
    //        logger.error("error handling stuff Eth", e)
    //      }
    //    )
    //
    //    logger.info("started eth")
    while (true) {
      val lastBlockEth = web3DAO.getLastBlockNumber(Ethereum).longValue()
      val lastBlockPolygon = web3DAO.getLastBlockNumber(Polygon).longValue()
      val lastBlockBsc = web3DAO.getLastBlockNumber(Bsc).longValue()

      lastBlockIndexedEth = indexBlockOfChain(lastBlockEth, lastBlockIndexedEth, 1, SupportedChains.Ethereum)
      lastBlockIndexedPolygon = indexBlockOfChain(lastBlockPolygon, lastBlockIndexedPolygon, 5, SupportedChains.Polygon)
      lastBlockIndexedBsc = indexBlockOfChain(lastBlockBsc, lastBlockIndexedBsc, 5, SupportedChains.Bsc)

      Thread.sleep(100)

    }

    //            web3Connector.web3Polygon.replayPastAndFutureBlocksFlowable(new DefaultBlockParameterNumber(lastBlockIndexedPolygon), false).subscribe(
    //              (e: EthBlock) => {
    //                indexBlockOfChainInReplay(e, 5, SupportedChains.Polygon)
    //              },
    //              (e: Throwable) => {
    //                logger.error("error handling stuff polygon", e)
    //              }
    //            )
  }

  /**
    * @param lastBlock Number of the last block found on the blockchain
    * @param lastBlockIndexed Number of the last block indexed by the service
    * @param numBlockToKeep Period of how many blocks should be stored. 1 => store all of them, 5 => keep one in five
    * @return
    */
  def indexBlockOfChain(lastBlock: Long, lastBlockIndexed: Long, numBlockToKeep: Int, chain: SupportedChains): Long = {
    if (lastBlock > lastBlockIndexed) {
      for (i <- lastBlockIndexed + 1 to lastBlock) {
        if (i % numBlockToKeep == 0) {
          try {
            logger.debug(s"Operating on $chain block $i")
            val block = web3DAO.getBlock(i, chain)
            val blockNumber = block.getNumber.intValue()
            val blockTs = block.getTimestamp.intValue()
            sqlBlocksDAO.addBlock(SqlBlock(blockNumber, blockTs))(chain)
            if (blockNumber % 500 == 0) {
              logger.info(s"indexed $chain: " + blockNumber.toString + " ts: " + blockTs.toString)
            }
          } catch {
            case e: Throwable =>
              logger.debug(s"$chain: error getting block $i: " + e.getMessage)
              Thread.sleep(100)
          }
        }

      }
      lastBlock
    } else {
      lastBlockIndexed
    }
  }

  def indexBlockOfChainInReplay(e: EthBlock, numBlockToKeep: Int, chain: SupportedChains) = {
    try {
      val block = e.getBlock
      val blockNumber = block.getNumber.intValue()
      if (blockNumber % numBlockToKeep == 0) {
        logger.debug(s"Operating on $chain block $blockNumber")
        val blockTs = block.getTimestamp.intValue()
        sqlBlocksDAO.addBlock(SqlBlock(blockNumber, blockTs))(chain)
        if (blockNumber % 500 == 0) {
          logger.info(s"indexed $chain: " + blockNumber.toString + " ts: " + blockTs.toString)
        }
      }
    } catch {
      case e: Throwable =>
        logger.debug(s"$chain: error getting latest block: " + e.getMessage)
        Thread.sleep(100)
    }

  }

}
