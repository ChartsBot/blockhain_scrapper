package com.chartsbot.models

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.MySQLConnector
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ CamelCase, MysqlAsyncContext, Ord }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SqlBlocksQueries {

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]]

  def insertBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]]

}

object SqlQueriesUtil extends LazyLogging {
  def transformFuture[T](future: Future[Right[Nothing, T]], errorLog: String)(implicit ec: ExecutionContext): Future[Either[ErrorMessage, T]] = {

    future transformWith {
      case Success(value) => Future.successful(value)
      case Failure(exception) =>
        exception match {
          case e: MySQLException =>
            logger.info(errorLog + e.errorMessage.toString)
            Future.successful(Left(e.errorMessage))
        }
    }
  }
}

@Singleton
class SqlBlocksEthQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("EthBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def insertBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]] = {
    val f = run(quote(query[SqlBlock].insert(lift(block)).onConflictIgnore)).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error inserting block ")
  }

}

@Singleton
class SqlBlocksPolygonQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("PolygonBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def insertBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]] = {
    val f = run(quote(query[SqlBlock].insert(lift(block)).onConflictIgnore)).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error inserting block ")
  }

}

@Singleton
class SqlBlocksBscQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("BscBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def insertBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]] = {
    val f = run(quote(query[SqlBlock].insert(lift(block)).onConflictIgnore)).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error inserting block ")
  }

}

trait SqlBlocksDAO {

  def getLastBlock(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]]

  def addBlock(block: SqlBlock)(chain: SupportedChains): Future[Either[ErrorMessage, Long]]

  def areLastBlocksIndexed(secondsFrom: Int): Future[Set[Boolean]]

}

@Singleton
class DefaultSqlBlocksDAO @Inject() (val sqlBlocksEth: SqlBlocksEthQueries, val sqlBlocksPolygon: SqlBlocksPolygonQueries, val sqlBlocksBsc: SqlBlocksBscQueries, implicit val ec: ExecutionContext) extends SqlBlocksDAO with LazyLogging {

  private def selectChainSqlQueries(chain: SupportedChains): SqlBlocksQueries = {
    chain match {
      case com.chartsbot.models.SupportedChains.Polygon => sqlBlocksPolygon
      case com.chartsbot.models.SupportedChains.Ethereum => sqlBlocksEth
      case com.chartsbot.models.SupportedChains.Bsc => sqlBlocksBsc
    }
  }

  def getLastBlock(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]] = selectChainSqlQueries(chain).getLast

  def addBlock(block: SqlBlock)(chain: SupportedChains): Future[Either[ErrorMessage, Long]] = selectChainSqlQueries(chain).insertBlock(block)

  def areLastBlocksIndexed(secondsFrom: Int): Future[Set[Boolean]] = {

    val currentTime = (System.currentTimeMillis / 1000).toInt

    val r: Set[Future[Boolean]] = for (chain <- SupportedChains.values) yield {
      getLastBlock(chain).map {
        case Left(value) => {
          logger.warn(s"$chain - error on healthchecks, failed to get last indexed block")
          false
        }
        case Right(value) => {
          val r = currentTime - value.head.blockTimestamp < secondsFrom
          if (!r) {
            logger.warn(s"$chain - error on healthchecks: last block indexed more than ${secondsFrom}s ago (ts = ${value.head.blockTimestamp}, current time = $currentTime)")
          }
          r
        }


      }
    }

    Future.sequence(r)
  }

}

