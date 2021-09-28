package com.chartsbot.services

import com.chartsbot.models.{ SqlBlock, SqlBlocksEthDAO, SqlBlocksPolygonDAO }
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.response.EthBlock

import javax.inject.{ Inject, Singleton }
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.implicitConversions

trait BlockIndexerService {

  def run(): Unit

}

@Singleton
class DefaultBlockIndexerService @Inject() (web3Connector: Web3Connector, sqlBlocksEthDAO: SqlBlocksEthDAO, sqlBlocksPolygonDAO: SqlBlocksPolygonDAO)(implicit val ec: ExecutionContext) extends BlockIndexerService with LazyLogging {

  val initBlockNumberPolygon: Long = Await.result(sqlBlocksPolygonDAO.getLastBlock, 10.seconds) match {
    case Left(value) =>
      logger.error("Error getting last indexed polygon block: ", value)
      throw new Exception("Not good getting last indexed block polygon")
    case Right(value) => value.head.number
  }

  val initBlockNumberEth: Long = Await.result(sqlBlocksEthDAO.getLastBlock, 10.seconds) match {
    case Left(value) =>
      logger.error("Error getting last indexed eth block: ", value)
      throw new Exception("Not good getting last indexed block eth")
    case Right(value) => value.head.number
  }

  def run(): Unit = {
    val blockParamPolygon = new DefaultBlockParameterNumber(initBlockNumberPolygon)
    val blockParamEth = new DefaultBlockParameterNumber(initBlockNumberEth)

    val goFastBlockNumberEthUntil = 12812350
    val goFastBlockNumberPolygonUntil = 18599632

    var t0 = System.currentTimeMillis()
    if (initBlockNumberPolygon < goFastBlockNumberPolygonUntil) {

      (initBlockNumberPolygon to goFastBlockNumberPolygonUntil).filter(_ % 50 == 0).foreach { i =>

        val block = web3Connector.web3Polygon.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()

        val sqlBlock = SqlBlock(i.intValue(), block.getBlock.getTimestamp.intValue())
        val r = sqlBlocksPolygonDAO.addBlock(sqlBlock)
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

    if (initBlockNumberEth < goFastBlockNumberEthUntil) {
      (initBlockNumberEth to goFastBlockNumberEthUntil).filter(_ % 50 == 0).foreach { i =>

        val block = web3Connector.web3Eth.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), false).send()

        val sqlBlock = SqlBlock(i.intValue(), block.getBlock.getTimestamp.intValue())
        val r = sqlBlocksEthDAO.addBlock(sqlBlock)
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

    web3Connector.web3Polygon.replayPastAndFutureBlocksFlowable(blockParamPolygon, false).subscribe(
      (e: EthBlock) => {
        val blockNumber = e.getBlock.getNumber.intValue()
        val blockTs = e.getBlock.getTimestamp.intValue()
        sqlBlocksPolygonDAO.addBlock(SqlBlock(blockNumber, blockTs))
        if (blockNumber % 500 == 0) {
          logger.info("indexed polygon: " + blockNumber.toString + " ts: " + blockTs.toString)
        } else {
        }
      }
    )

    web3Connector.web3Eth.replayPastAndFutureBlocksFlowable(blockParamEth, false).subscribe(
      (e: EthBlock) => {
        val blockNumber = e.getBlock.getNumber.intValue()
        val blockTs = e.getBlock.getTimestamp.intValue()
        sqlBlocksEthDAO.addBlock(SqlBlock(blockNumber, blockTs))
        if (blockNumber % 500 == 0) {
          logger.info("indexed eth: " + blockNumber.toString + " ts: " + blockTs.toString)
        } else {
        }
      }
    )

  }

}
