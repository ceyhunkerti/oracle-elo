package io.blue.service

import java.util.Date
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

import scala.annotation.meta.setter
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.pattern.{Patterns,ask}

import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Pageable, Page}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.exception.ExceptionUtils

import com.zaxxer.hikari.{HikariDataSource, HikariConfig}

import io.blue.AppInit
import io.blue.model._
import io.blue.actor.message._
import io.blue.exception._
import io.blue.config.EloConfig



@Service
@Transactional
class ConnectionService {

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var appInit: AppInit = _

  @(Autowired @setter)
  private var eloConfig: EloConfig = _

  private var dataSource: HikariDataSource = _

  @PostConstruct
	def init {
    initDataSource
	}

  def initDataSource {
    try {
      var config = new HikariConfig
      config.setJdbcUrl(eloConfig.url)
      config.setUsername(eloConfig.username)
      config.setPassword(eloConfig.password)
      config.setMinimumIdle(10)
      dataSource = new HikariDataSource(config)
    } catch {
      case e: Exception =>
        logger.error(ExceptionUtils.getStackTrace(e))
    }
  }

  def getConnection: java.sql.Connection = {
    try {
      dataSource.getConnection
    } catch {
      case e: Exception =>
        logger.warn("Retring connection initialization ...")
        initDataSource
        dataSource.getConnection
    }
  }


}
