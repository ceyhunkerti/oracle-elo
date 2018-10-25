package io.blue.service

import javax.annotation.PostConstruct
import java.text.NumberFormat
import java.util.UUID
import java.io.File
import scala.io.Source
import scala.collection.JavaConversions._
import scala.annotation.meta.setter
import org.springframework.scheduling.annotation.Async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.apache.commons.io.{ IOUtils, FileUtils }
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.lang3.exception.ExceptionUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

import io.blue.model._
import io.blue.model.pl._
import io.blue.actor.message._
import io.blue.AppInit

@Service
@Transactional
class PlService{

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var appInit: AppInit = _

  @(Autowired @setter)
  private var connectionService: ConnectionService = _

  def getConnection: java.sql.Connection = {
    connectionService.getConnection
  }

  def findLogs(limit: java.lang.Long = 1000) = {
    var result: List[PlLog] = List()
    val q = """
      select name, log_level, start_date, end_date,message, statement
      from
        (select /*+ parallel(8) */
          name, log_level, start_date, end_date,message, statement,
          row_number() over(order by start_date desc) rank_id
        from logs)
      where rank_id <= ?
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val stmt = connection.prepareStatement(q)
      stmt.setFetchSize(1000)
      stmt.setLong(1, limit)
      val rs = stmt.executeQuery
      while(rs.next) {
        var log = new PlLog
        log.name = rs.getString("NAME")
        log.level= rs.getString("LOG_LEVEL")
        log.startTime = rs.getTimestamp("START_DATE")
        log.endTime = rs.getTimestamp("END_DATE")
        log.message = rs.getString("MESSAGE")
        log.statement = rs.getString("STATEMENT")
        result ::= log
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result.sortBy(- _.startTime.getTime)
  }

}