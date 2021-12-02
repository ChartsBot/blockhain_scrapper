package com.chartsbot

import com.chartsbot.config.ConfigPaths.ServerPaths
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.{ Handler, HttpConnectionFactory, Server }
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

import javax.inject.{ Inject, Singleton }

trait JettyServer {
  def start(): Unit
}

@Singleton
class DefaultJettyServer @Inject() (config: Config) extends JettyServer with LazyLogging with ServerPaths {

  val serverPort: Int = config.getInt(SERVER_PORT)
  val serverBaseUrl: String = config.getString(SERVER_BASE_URL)
  val appVersion: String = config.getString(APP_VERSION)

  val contextPathBase: String = serverBaseUrl + "/" + appVersion

  def disableServerVersionHeader(server: Server): Unit = {
    server.getConnectors.foreach { connector =>
      connector.getConnectionFactories
        .stream()
        .filter(cf => cf.isInstanceOf[HttpConnectionFactory])
        .forEach(cf => cf.asInstanceOf[HttpConnectionFactory].getHttpConfiguration.setSendServerVersion(false))
    }
  }

  private def initializeServer: Server = {
    val server = createServer
    disableServerVersionHeader(server)
    val contexts = createContextsOfTheServer

    server.setHandler(contexts)
    server
  }

  def start(): Unit = {
    try {
      val server = initializeServer
      server.start()
      server.join()
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        System.exit(-1)
    }
  }

  private def createServer = {
    new Server(serverPort)
  }

  private def createContextsOfTheServer = {
    val contextRestApi: WebAppContext = createContextScalatraApi
    initialiseContextHandlerCollection(Array(contextRestApi))
  }

  private def initialiseContextHandlerCollection(contexts: Array[Handler]): ContextHandlerCollection = {
    val contextCollection = new ContextHandlerCollection()
    contextCollection.setHandlers(contexts)
    contextCollection
  }

  private def createContextScalatraApi: WebAppContext = {
    val contextRestApi = new WebAppContext()
    contextRestApi.setContextPath(contextPathBase)
    contextRestApi.setResourceBase("src/main/scala")
    contextRestApi.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")
    contextRestApi.addEventListener(new ScalatraListener)
    contextRestApi.addServlet(classOf[DefaultServlet], "/")
    contextRestApi
  }
}
