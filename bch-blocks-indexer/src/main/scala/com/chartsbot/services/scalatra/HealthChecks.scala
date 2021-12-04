package com.chartsbot.services.scalatra

import com.chartsbot.models.{SqlBlock, SqlBlocksDAO, SupportedChains, Web3DAO}
import com.chartsbot.models.SupportedChains.SupportedChains
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.{FutureSupport, InternalServerError, Ok, ScalatraServlet}
import org.scalatra.json.NativeJsonSupport

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

class HealthChecks @Inject() (conf: Config, sqlBlocksDAO: SqlBlocksDAO, web3DAO: Web3DAO)(implicit val ec: ExecutionContext) extends ScalatraServlet
  with FutureSupport with NativeJsonSupport with LazyLogging {

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  override protected implicit def executor: ExecutionContext = ec

  before() {
    contentType = formats("json")
  }

  logger.info("Health check server started")

  get("/ready") {
    Ok("ready")
  }

  get("/alive") {
    val isWeb3Working = web3DAO.isAlive(SupportedChains.Ethereum)
    val isSqlWorkingAndAreLastBlocksIndexed: Boolean = !Await.result(sqlBlocksDAO.areLastBlocksIndexed(120), 10.seconds).contains(false)

    if (isSqlWorkingAndAreLastBlocksIndexed && isWeb3Working) {
      Ok("alive")
    } else {
      InternalServerError("Not Ok")
    }
  }

  def getLastIndexedBlock(chain: SupportedChains): Either[ErrorMessage, List[SqlBlock]] = Await.result(sqlBlocksDAO.getLastBlock(chain), 10.seconds)


}
