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

import io.blue.model.meta._
import io.blue.actor.message._
import io.blue.AppInit

@Service
@Transactional
class MetaService{

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var appInit: AppInit = _

  @(Autowired @setter)
  private var connectionService: ConnectionService = _

  def getConnection: java.sql.Connection = {
    connectionService.getConnection
  }

  def createTable(table: MetaTable) = {
    val q = s"""
      create table ${table.owner}.${table.name}
      ${if(table.options != null) table.options else ""}
      as
      select
        ${table.columns.mkString(",")}
      from
        ${table.sourceSchema}.${table.sourceTable}@${table.dbLink}
      where 1 = 2
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      logger.debug(q)
      connection.createStatement.executeUpdate(q)
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    table
  }

}