package io.blue.service

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

import io.blue.model._
import io.blue.actor.message._
import io.blue.AppInit


@Service
@Transactional
class AppService{

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var appInit: AppInit = _

  @(Autowired @setter)
  private var connectionService: ConnectionService = _

  def version = {
    case class Version(date: String, major: String, minor: String, versionCode: String, patch: String)
    val lines = Source.fromFile("version.properties").getLines.toList
    Version(lines(0),lines(1),lines(2),lines(3),lines(4))
  }

  def test = {
    appInit.system.actorSelection("/user/mail") ! SendSessionBlockMail("demo")
  }
}