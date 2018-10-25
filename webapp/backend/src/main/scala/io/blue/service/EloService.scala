package io.blue.service

import scala.collection.mutable.ListBuffer
import java.text.NumberFormat
import java.util.UUID
import java.io.File
import scala.io.Source
import scala.collection.JavaConversions._
import scala.annotation.meta.setter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.apache.commons.io.{ IOUtils, FileUtils }
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.lang3.exception.ExceptionUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

import io.blue.AppInit
import io.blue.model._
import io.blue.model.elo._
import io.blue.actor.message._
import io.blue.util._

@Service
@Transactional
class EloService{

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var appInit: AppInit = _

  @(Autowired @setter)
  private var connectionService: ConnectionService = _

  @(Autowired @setter)
  private var asyncRunner: AsyncRunner = _


  def getConnection: java.sql.Connection = {
    connectionService.getConnection
  }

  def findEloStatus = {
    case class Status(
      success: Int = 0,
      error: Int = 0,
      ready: Int = 0,
      running: Int = 0,
      total: Int = 0,
      startTime: Long = 0,
      endTime: Long = 0
    )
    var result: Status =  null
    val q = """
      select
        sum(case when status = 'SUCCESS' then 1 else 0 end) success,
        sum(case when status = 'ERROR' then 1 else 0 end) error,
        sum(case when status = 'READY' then 1 else 0 end) ready,
        sum(case when status = 'RUNNING' then 1 else 0 end) running,
        sum(1) total,
        min(start_time) start_time,
        max(end_time) end_time
      from elo_tables
      where status in ('SUCCESS', 'ERROR', 'READY', 'RUNNING') and excluded = 0
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) {
        result = new Status(
          rs.getInt("success"), rs.getInt("error"), rs.getInt("ready"),
          rs.getInt("running"), rs.getInt("total"),
          rs.getDate("start_time").getTime, rs.getDate("end_time").getTime
        )
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findEloSize = {
    var result: Int = 0
    val q = """
      select round(SUM(BYTES)/(1024*1024*1024)) GB from dba_segments where owner||'.'||segment_name in (
        select upper(target) from elo_tables where nvl(excluded, 0) = 0
      )
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val stmt = connection.createStatement
      val rs = stmt.executeQuery(q)
      while(rs.next) { result = rs.getInt(1) }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findDbLinks = {
    var result = List[String]()
    val q = "select db_link from all_db_links"
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) { result ::= rs.getString(1) }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findSchemas(prefix: String = "") = {
    var result = List[String]()
    val q = s"select username from all_users${prefix}"
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) { result ::= rs.getString(1) }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findTables(owner: String, prefix: String = "") = {
    var result = List[String]()
    val q = s"select table_name from all_tables${prefix} where upper(owner) = upper('${owner}')"
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) { result ::= rs.getString(1) }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findColumns(owner: String, table: String, prefix: String = "") = {
    var result = List[String]()
    val q = s"""
      select /*+ parallel(4) */ column_name from all_tab_cols${prefix}
      where
      hidden_column = 'NO' and
      upper(owner) = upper('${owner}') and upper(table_name) = upper('${table}')
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) { result ::= rs.getString(1) }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findAllTableColumns(prefix: String = "") = {
    var result = Map[(String, String), ListBuffer[String]]()
    val q = s"""
      select /*+ parallel(16) */ owner, table_name, column_name
      from all_tab_cols${prefix}
      where
      hidden_column = 'NO' and
      owner || '.' || table_name in (
        select /*+ parallel(16) */ upper(target) from elo_tables
      )
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      logger.debug(q)
      val stmt = connection.createStatement
      stmt.setFetchSize(10000)
      val rs = stmt.executeQuery(q)
      while(rs.next) {
        val owner= rs.getString("OWNER")
        val name = rs.getString("TABLE_NAME")
        val column = rs.getString("COLUMN_NAME")

        result get (owner, name) match {
          case Some(columns) => columns += column
          case None => result += ((owner, name) -> ListBuffer[String](column))
        }
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findSourceSchemas(dbLink: String) = findSchemas(s"@${dbLink}")
  def findSourceTables(dbLink: String, owner: String) = findTables(owner, s"@${dbLink}")
  def findSourceColumns(dbLink: String, owner: String, table: String) = findColumns(owner, table, s"@${dbLink}")

  def findTargetSchemas = findSchemas()
  def findTargetTables(owner: String) = findTables(owner)
  def findTargetColumns(owner: String, table: String) = findColumns(owner, table)


  def findEloMapColumns = {
    var result = scala.collection.mutable.Map[String, ListBuffer[EloColumn]]()
    val q = s"""
      select name, source_col, target_col, excluded from elo_columns
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val stmt = connection.createStatement
      stmt.setFetchSize(10000)
      val rs = stmt.executeQuery(q)

      while(rs.next) {
        var column = new EloColumn
        column.name = rs.getString("NAME")
        column.expression = rs.getString("SOURCE_COL")
        column.target = rs.getString("TARGET_COL")
        column.excluded = rs.getInt("EXCLUDED") != 0
        result get column.name match {
          case Some(columns) =>
            columns += column
          case None =>
            var columns = ListBuffer[EloColumn](column)
            result += (column.name -> columns)
        }
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findEloColumns(name: String) = {
    var result = List[EloColumn]()
    val q = s"""
      select name, source_col, target_col, excluded
      from elo_columns where upper(name) = upper('${name}')
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) {
        var column = new EloColumn
        column.name = rs.getString("NAME")
        column.expression = rs.getString("SOURCE_COL")
        column.target = rs.getString("TARGET_COL")
        column.excluded = rs.getInt("EXCLUDED") != 0
        result ::= column
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findEloOwners = {
    var result: List[String] = List()
    var connection: java.sql.Connection = null
    val q = """
      select distinct substr(target, 1, instr(target, '.')-1) owner from elo_tables
    """
    try {
        connection = getConnection
        val rs = connection.createStatement.executeQuery(q)
        while(rs.next) {
          result ::= rs.getString("OWNER")
        }
        connection.close
      } catch {
        case e: Exception =>
          logger.error(ExceptionUtils.getStackTrace(e))
          if (connection != null) { connection.close }
          throw e
      }
      result
  }

  def findMaps = {
    var result: List[EloMap]  = List()
    val q = s"""
      select /*+ parallel(4) */
        name, db_link, t.source, target, t.filter, t.source_hint,
        target_hint,delta_column,last_delta,excluded,status, analyze,
        drop_create
      from elo_tables t
    """
    val mc = findEloMapColumns

    logger.debug("Ready! Finding maps ...")

    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val stmt = connection.createStatement
      stmt.setFetchSize(1000)
      val rs = stmt.executeQuery(q)
      while(rs.next) {
        var m = new EloMap
        m.name = rs.getString("NAME")
        m.dbLink = rs.getString("DB_LINK")
        m.sourceSchema = {
          var r = rs.getString("SOURCE").split("\\.")
          if (r.length > 0) r(0) else null }
        m.sourceTable = {
          var r = rs.getString("SOURCE").split("\\.")
          if (r.length > 0) r(1) else r(0) }
        m.targetSchema = {
          var r = rs.getString("TARGET").split("\\.")
          if (r.length > 0) r(0) else null }
        m.targetTable = {
          var r = rs.getString("TARGET").split("\\.")
          if (r.length > 0) r(1) else r(0) }
        m.filter = rs.getString("FILTER")
        m.sourceHint = rs.getString("SOURCE_HINT")
        m.targetHint = rs.getString("TARGET_HINT")
        m.deltaColumn = rs.getString("DELTA_COLUMN")
        m.lastDelta = rs.getString("LAST_DELTA")
        m.excluded = rs.getInt("EXCLUDED") != 0
        m.dropCreate = rs.getInt("DROP_CREATE") != 0
        m.status = rs.getString("STATUS")

        m.columns = (mc getOrElse (m.name, List[EloColumn]())).toList
        result ::= m
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def findMap(name: String) = {
    var result = new EloMap
    val q = s"""
      select
        name, db_link, t.source, target, t.filter, t.source_hint,
        target_hint,delta_column,last_delta,excluded,status, drop_create,
        analyze
      from elo_tables t
      where upper(name) = upper('${name}')
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) {
        result.name = rs.getString("NAME")
        result.dbLink = rs.getString("DB_LINK")
        result.sourceSchema = {
          var r = rs.getString("SOURCE").split("\\.")
          if (r.length > 0) r(0) else null }
        result.sourceTable = {
          var r = rs.getString("SOURCE").split("\\.")
          if (r.length > 0) r(1) else r(0) }
        result.targetSchema = {
          var r = rs.getString("TARGET").split("\\.")
          if (r.length > 0) r(0) else null }
        result.targetTable = {
          var r = rs.getString("TARGET").split("\\.")
          if (r.length > 0) r(1) else r(0) }
        result.filter = rs.getString("FILTER")
        result.sourceHint = rs.getString("SOURCE_HINT")
        result.targetHint = rs.getString("TARGET_HINT")
        result.deltaColumn = rs.getString("DELTA_COLUMN")
        result.lastDelta = rs.getString("LAST_DELTA")
        result.excluded = rs.getInt("EXCLUDED") != 0
        result.dropCreate = rs.getInt("DROP_CREATE") != 0
        result.analyze = rs.getInt("ANALYZE") != 0
        result.status = rs.getString("STATUS")
      }

      result.columns = findEloColumns(name)
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result
  }

  def isEloMapExist(name: String): Boolean = {
    var result: Int = 0
    val q = s"""
      select count(1) from elo_tables where upper(name) = upper('${name}')
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      val rs = connection.createStatement.executeQuery(q)
      while(rs.next) {
        logger.debug(s"$result")
        result = rs.getInt(1)
      }
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    result != 0
  }

  private def insertColum(name: String, c: EloColumn) {
    val q = s"""
      insert into elo_columns (
        name, source_col, target_col, excluded
      ) values (
        '${name}',
        '${c.expression}',
        '${c.target}',
         ${if (c.excluded) 1 else 0 }
      )
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
  }

  def deleteMap(name: String) = {
    val map = findMap(name)
    var connection: java.sql.Connection = null
    var q1 = s"""
      delete from elo_tables where upper(name) = upper('${name}')
    """
    var q2 = s"""
      delete from elo_columns where upper(name) = upper('${name}')
    """
    try {
      connection = getConnection
      logger.debug(q1)
      connection.createStatement.executeUpdate(q1)
      logger.debug(q1)
      connection.createStatement.executeUpdate(q2)
      connection.close
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    map
  }

  def updateMap(m: EloMap) = {
    deleteMap(m.name)
    createMap(m)
  }

  @throws(classOf[RuntimeException])
  def createMap(m: EloMap) = {
    if (m.name == null || m.name.isEmpty) {
      throw new RuntimeException(s"""Map name can not be empty!""")
    }
    if (isEloMapExist(m.name)) {
      throw new RuntimeException(s"""Map already exists!""")
    }
    val q = s"""
      insert into elo_tables (
        name, db_link, source, target, filter,
        source_hint, target_hint, delta_column, last_delta, excluded, drop_create,
        analyze
      ) values (
        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
      )
    """
    var connection: java.sql.Connection = null
    try {
      connection = getConnection
      logger.debug(q)
      val stmt = connection.prepareStatement(q)
      stmt.setString(1, m.name.toUpperCase)
      stmt.setString(2, m.dbLink)
      stmt.setString(3, s"${m.sourceSchema}.${m.sourceTable}")
      stmt.setString(4, s"${m.targetSchema}.${m.targetTable}")
      stmt.setString(5, m.filter)
      stmt.setString(6, m.sourceHint)
      stmt.setString(7, m.targetHint)
      stmt.setString(8, m.deltaColumn)
      stmt.setString(9, m.lastDelta)
      stmt.setInt(10, if (m.excluded) 1 else 0)
      stmt.setInt(11, if (m.dropCreate) 1 else 0)
      stmt.setInt(12, if (m.analyze) 1 else 0)
      stmt.executeUpdate
      connection.close
      // m.columns.filter(_.mapped).foreach(insertColum(m.name.toUpperCase, _))
      m.columns.foreach(insertColum(m.name.toUpperCase, _))
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
        if (connection != null) { connection.close }
        throw e
    }
    findMap(m.name)
  }

  def excludeMap(name: String, excluded: Boolean = true) = {
    var connection: java.sql.Connection = null
    val q = s"""
      update elo_tables set excluded = ${if(excluded) 1 else 0} where upper(name) = upper('${name}')
    """
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
    findMap(name)
  }

  def excludeMaps(names: List[String], excluded: Boolean = true) = {
    names.map(excludeMap(_, excluded))
  }

  def run(name: String) = {
    logger.info(s"Will run ${name} ....")
    runAsync(name)
    logger.info(s"Running ${name} ....")
    true
  }

  def runAsync(name: String) {
    asyncRunner.run(getConnection, name)
  }

}