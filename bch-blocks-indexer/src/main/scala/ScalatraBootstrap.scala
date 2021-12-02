import com.chartsbot.Service
import com.chartsbot.config.ConfigPaths.ScalatraPaths
import com.chartsbot.services.scalatra.HealthChecks
import org.scalatra.LifeCycle

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle with ScalatraPaths {

  lazy val healthChecks: HealthChecks = Service.get[HealthChecks]

  override def init(context: ServletContext): Unit = {

    context.setInitParameter("org.scalatra.environment", "development")

    context.mount(healthChecks, "/health", "HealthChecks")
  }
}
