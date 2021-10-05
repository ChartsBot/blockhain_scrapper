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
    // Dirty stuff until new version of web3j includes this PR https://github.com/web3j/web3j/pull/1507
    while (true) {
      val lastBlockEth = web3DAO.getLastBlockNumber(Ethereum).longValue()
      val lastBlockPolygon = web3DAO.getLastBlockNumber(Polygon).longValue()
      val lastBlockBsc = web3DAO.getLastBlockNumber(Bsc).longValue()

      if (lastBlockEth > lastBlockIndexedEth) {
        for (i <- lastBlockIndexedEth + 1 to lastBlockEth) {
          try {

            logger.debug("Operating on eth block " + i)
            val block = web3DAO.getBlock(i, SupportedChains.Ethereum)
            val blockNumber = block.getNumber.intValue()
            val blockTs = block.getTimestamp.intValue()
            sqlBlocksDAO.addBlock(SqlBlock(blockNumber, blockTs))(Ethereum)
            if (blockNumber % 500 == 0) {
              logger.info("indexed eth: " + blockNumber.toString + " ts: " + blockTs.toString)
            }
          } catch {
            case e: Throwable =>
              logger.debug(s"Eth: error getting block $i: " + e.getMessage)
              Thread.sleep(100)
          }

        }
        lastBlockIndexedEth = lastBlockEth
      }

      if (lastBlockPolygon > lastBlockIndexedPolygon) {
        for (i <- lastBlockIndexedPolygon + 1 to lastBlockPolygon) {
          if (i % 5 == 0) {
            try {
              logger.debug("Operating on polygon block " + i)
              val block = web3DAO.getBlock(i, SupportedChains.Polygon)
              val blockNumber = block.getNumber.intValue()
              val blockTs = block.getTimestamp.intValue()
              sqlBlocksDAO.addBlock(SqlBlock(blockNumber, blockTs))(Polygon)
              if (blockNumber % 500 == 0) {
                logger.info("indexed polygon: " + blockNumber.toString + " ts: " + blockTs.toString)
              }
            } catch {
              case e: Throwable =>
                logger.debug(s"Polygon: error getting block $i: " + e.getMessage)
                Thread.sleep(100)
            }
          }

        }
        lastBlockIndexedPolygon = lastBlockPolygon
      }

      if (lastBlockBsc > lastBlockIndexedBsc) {
        for (i <- lastBlockIndexedBsc + 1 to lastBlockBsc) {
          if (i % 5 == 0) {
            try {
              logger.debug("Operating on bsc block " + i)
              val block = web3DAO.getBlock(i, SupportedChains.Bsc)
              val blockNumber = block.getNumber.intValue()
              val blockTs = block.getTimestamp.intValue()
              sqlBlocksDAO.addBlock(SqlBlock(blockNumber, blockTs))(Bsc)
              if (blockNumber % 500 == 0) {
                logger.info("indexed bsc: " + blockNumber.toString + " ts: " + blockTs.toString)
              }
            } catch {
              case e: Throwable =>
                logger.debug(s"Bsc: error getting block $i: " + e.getMessage)
                Thread.sleep(100)
            }
          }

        }
        lastBlockIndexedBsc = lastBlockBsc
      }
      Thread.sleep(100)

    }

    //    web3Connector.web3Polygon.replayPastAndFutureBlocksFlowable(blockParamPolygonNew, false).subscribe(
    //      (e: EthBlock) => {
    //        val blockNumber = e.getBlock.getNumber.intValue()
    //        val blockTs = e.getBlock.getTimestamp.intValue()
    //        sqlBlocksPolygonDAO.addBlock(SqlBlock(blockNumber, blockTs))
    //        if (blockNumber % 500 == 0) {
    //          logger.info("indexed polygon: " + blockNumber.toString + " ts: " + blockTs.toString)
    //        } else {
    //        }
    //      },
    //      (e: Throwable) => {
    //        logger.error("error handling stuff polygon", e)
    //      }
    //    )
    //
    //    web3Connector.web3Eth.replayPastAndFutureBlocksFlowable(blockParamEthNew, false).subscribe(
    //      (e: EthBlock) => {
    //        val blockNumber = e.getBlock.getNumber.intValue()
    //        val blockTs = e.getBlock.getTimestamp.intValue()
    //        sqlBlocksEthDAO.addBlock(SqlBlock(blockNumber, blockTs))
    //        if (blockNumber % 500 == 0) {
    //          logger.info("indexed eth: " + blockNumber.toString + " ts: " + blockTs.toString)
    //        } else {
    //        }
    //      },
    //      (e: Throwable) => {
    //        logger.error("error handling stuff eth", e)
    //      }
    //    )

  }

}
