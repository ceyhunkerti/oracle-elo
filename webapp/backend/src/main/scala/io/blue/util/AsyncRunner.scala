package io.blue.util

import org.springframework.scheduling.annotation.Async
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component

@EnableAsync
@Component
class AsyncRunner {

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @Async
  def run(connection: java.sql.Connection, name: String) {
    val q = s"""
      {call util.elo.run(?)}
    """
    try {
      logger.debug(q)
      val stmt = connection.prepareCall(q)
      stmt.setString(1, name)
      stmt.execute
      stmt.close
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
  }

}