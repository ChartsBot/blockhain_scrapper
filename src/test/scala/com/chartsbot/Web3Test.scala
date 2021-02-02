package com.chartsbot

import com.chartsbot.services.Web3Connector
import org.scalatest.BeforeAndAfterEach
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterNumber}

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.math.BigInt.javaBigInteger2bigInt

class Web3Test extends AnyFeatureSpecLike with Matchers with BeforeAndAfterEach {

  val InjectorTests: InjectorHelper = new InjectorHelper(List(new Binder {
  })) {}

  val web3: Web3j = InjectorTests.get[Web3Connector].web3

  Feature("web3 connection") {
    Scenario("Web3 should connect to the node and get the last block") {
      val fRes = web3.ethBlockNumber().sendAsync().toScala
      val res = Await.result(fRes, 10.second)
      res.getBlockNumber > 11778992 shouldBe true
    }
  }

  Feature("get the block info") {
    Scenario("Web3 client should get random block info (hash, timestamp)") {
      val lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber
      val randomBlockNumber = scala.util.Random.nextInt(lastBlockNumber.toInt)
      val block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(randomBlockNumber), false).send().getBlock
      val hash = block.getHash
      val timestamp = block.getTimestamp
      println(block.getNumber)
      println(hash)
      println(timestamp)
      block.getNumber.toInt shouldBe randomBlockNumber
    }
  }

}
