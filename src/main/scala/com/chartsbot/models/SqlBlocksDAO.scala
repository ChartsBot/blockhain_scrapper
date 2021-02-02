package com.chartsbot.models

import com.chartsbot.services.MySQLConnector
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ CamelCase, MysqlAsyncContext, Ord }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class SqlBlocksQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("EthBlocks")

  def transformFuture[T](future: Future[Right[Nothing, T]], errorLog: String): Future[Either[ErrorMessage, T]] = {

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

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    transformFuture(f, "SQL error getting last block ")
  }

  def insertBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]] = {
    val f = run(quote(query[SqlBlock].insert(lift(block)))).map(Right(_))
    transformFuture(f, "SQL error inserting block ")
  }

}

trait SqlBlocksDAO {

  def getLastBlock: Future[Either[ErrorMessage, List[SqlBlock]]]

  def addBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]]

}

@Singleton
class DefaultSqlBlocksDAO @Inject() (val sqlBlocks: SqlBlocksQueries, implicit val ec: ExecutionContext) extends SqlBlocksDAO with LazyLogging {

  def getLastBlock: Future[Either[ErrorMessage, List[SqlBlock]]] = sqlBlocks.getLast

  def addBlock(block: SqlBlock): Future[Either[ErrorMessage, Long]] = sqlBlocks.insertBlock(block)

}

