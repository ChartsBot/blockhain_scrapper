package com.chartsbot.services

import com.chartsbot.models.{ SqlBlock, SqlBlocksDAO, Web3DAO }
import com.typesafe.scalalogging.LazyLogging

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.DurationInt

trait BlockIndexerService {

  def run(): Unit

}

@Singleton
class DefaultBlockIndexerService @Inject() (web3DAO: Web3DAO, sqlBlocksDAO: SqlBlocksDAO)(implicit val ec: ExecutionContext) extends BlockIndexerService with LazyLogging {

  def run(): Unit = {
    val fLastIndexedBlock = sqlBlocksDAO.getLastBlock

    val task = fLastIndexedBlock map {
      case Left(error) =>
        logger.error("Error getting first value: " + error.toString, new Exception("Error starting soft"))
      case Right(value) =>
        val lastIndexedBlock = value.head
        val lastIndexedBlockNumber = lastIndexedBlock.number
        logger.info("Last indexed block: " + lastIndexedBlockNumber)
        val fLastBlockEth = web3DAO.getLastBlock
        fLastBlockEth map { lastBlockEth =>
          for (i <- (lastIndexedBlockNumber + 1) to lastBlockEth) {
            val res = web3DAO.getBlock(i.toInt) flatMap { newBlock =>
              val sqlBlock = SqlBlock(
                number = newBlock.getBlock.getNumber.longValue(),
                hash = newBlock.getBlock.getHash,
                blockTimestamp = newBlock.getBlock.getTimestamp.intValue()
              )
              sqlBlocksDAO.addBlock(sqlBlock)
            }
            Await.result(res, 2.seconds)
            if (i % 25 == 0) {
              logger.info("OK block " + i.toString)
            }
          }
        }
    }
    Await.result(task, 1000.hour)
  }

}
