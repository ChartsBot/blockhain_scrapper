//package com.chartsbot
//
//import com.chartsbot.models.{ SqlBlock }
//import com.chartsbot.services.MySQLConnector
//import io.getquill.{ CamelCase, MysqlAsyncContext }
//import org.scalatest.BeforeAndAfterEach
//import org.scalatest.featurespec.AnyFeatureSpecLike
//import org.scalatest.matchers.should.Matchers
//
//import scala.concurrent.duration.DurationInt
//import scala.concurrent.{ Await, ExecutionContext }
//
//class SqlTest extends AnyFeatureSpecLike with Matchers with BeforeAndAfterEach {
//
//  val InjectorTests: InjectorHelper = new InjectorHelper(List(new Binder {
//  })) {}
//
//  val sqlConnector: MySQLConnector = InjectorTests.get[MySQLConnector]
//
//  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx
//
//  import ctx._
//
//  implicit val ec: ExecutionContext = InjectorTests.get[ExecutionContext]
//
//  Feature("Add block") {
//    Scenario("Add one block") {
//      val blockToAdd = gimmeBlock()
//      val dao = InjectorTests.get[SqlBlocksDAO]
//      val res = Await.result(dao.addBlock(blockToAdd), 100.second)
//      Thread.sleep(100)
//    }
//
//    Scenario("Add multiple blocks") {
//
//    }
//
//    Scenario("Add twice the same block -> FAIL") {
//
//    }
//  }
//
//  Feature("Read block") {
//    Scenario("Read one block") {
//      val blockToAdd = gimmeBlock(maybeNumber = Some(100))
//      val dao = InjectorTests.get[SqlBlocksDAO]
//      Await.result(dao.addBlock(blockToAdd), 100.second)
//      Thread.sleep(100)
//      val res2 = Await.result(dao.getLastBlock, 10.second)
//      res2 match {
//        case Left(_) => fail()
//        case Right(value) =>
//          value.length shouldBe 1
//          value.head shouldBe blockToAdd
//      }
//      Thread.sleep(100)
//      val blockToAdd2 = gimmeBlock(maybeNumber = Some(10032))
//      Await.result(dao.addBlock(blockToAdd2), 100.second)
//      Thread.sleep(100)
//      val res3 = Await.result(dao.getLastBlock, 10.second)
//      res3 match {
//        case Left(_) => fail()
//        case Right(value) =>
//          value.length shouldBe 1
//          value.head shouldBe blockToAdd2
//      }
//    }
//  }
//
//  def gimmeBlock(maybeNumber: Option[Long] = None, maybeHash: Option[String] = None, maybeTs: Option[Int] = None): SqlBlock = {
//    val number = maybeNumber.getOrElse(scala.util.Random.nextLong())
//    val hash = maybeHash.getOrElse(TestUtils.generateRandomString())
//    val ts = maybeTs.getOrElse(scala.util.Random.nextInt())
//    SqlBlock(
//      number = number,
//      hash = hash,
//      blockTimestamp = ts
//    )
//  }
//}
