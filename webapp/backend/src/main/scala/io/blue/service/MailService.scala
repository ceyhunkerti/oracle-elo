package io.blue.service


import java.util.Properties;

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.annotation.meta.setter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import io.blue.exception._
import io.blue.model._
import io.blue.exception.SystemUserNotAllowedException
import com.fasterxml.jackson.databind.ObjectMapper
import javax.mail.Session
import javax.mail.{Address,Message,MessagingException,PasswordAuthentication,Transport}
import javax.mail.internet.{InternetAddress,MimeMessage}
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

import org.apache.commons.lang3.exception.ExceptionUtils

@Service
class MailService {

  private val log:Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @(Autowired @setter)
  private var settingService: SettingService = _

  @(Autowired @setter)
  private var userService: UserService = _


  def test(s: MailSetting) = {
    var m = new Mail
    m.to = s.to
    m.from = s.from
    m.subject= "Test mail from sakura"
    m.body = "If you see this then the mail service works."
    sendMail(m, s, false)
  }

  def sendUserPasswordMail(user: User, password: String) = {
    var mail = new Mail
    val s = settingService.findMailSettings
    if(s != None && s.get.isActive){
      if(user.email != null){
        mail.to = user.email
        mail.subject= s"${user.name} welcome to Sakura"
        mail.body = s"username: ${user.name} password: ${password}"
        sendMail(mail)
      }
    }
  }

  def sendNewPasswordMail(user: User, password: String) = {
    var mail = new Mail
    val s = settingService.findMailSettings
    if(s != None && s.get.isActive){
      if(user.email != null){
        mail.to = user.email
        mail.subject= s"New password - Octopus"
        mail.body = s"username: ${user.name} new-password: ${password}"
        sendMail(mail)
      }
    }
  }

  def sendSessionBlockMail(message: String) = {
    settingService.findMailSettings match {
      case Some(s) =>
        var mail = new Mail
        mail.to = s.to
        mail.from = s.from
        mail.subject= s"Blocking sessions - Sakura"
        mail.body = message
        sendMail(mail, s)
      case _ =>
    }
  }

  def sendMail(m: Mail, s: MailSetting, silent: Boolean = true) {
    var p = new Properties
    p.put("mail.smtp.auth", "true")
    p.put("mail.smtp.host", s"${s.host}")
    p.put("mail.smtp.port", s"${s.port}")
    s.connectionSecurity.toLowerCase match {
      case "ssl" => p.put("mail.smtp.ssl.enable", "true")
      case "tls" => p.put("mail.smtp.starttls.enable", "true")
      case _ =>
    }
    val session = Session.getInstance(p,
      new javax.mail.Authenticator() {
        override protected def getPasswordAuthentication : PasswordAuthentication =
          new PasswordAuthentication(s.username, s.password)
      }
    )
    log.debug("Auth. mail server OK")
    try{
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(if(m.from!=null) m.from else s.from))

      m.to.split(",").foreach{m =>
        message.addRecipient(Message.RecipientType.TO,new InternetAddress(m))
      }
      message.setSubject(m.subject)
      message.setText(m.body)
      Transport.send(message)
    }catch {
      case e:Exception =>
        val msg = ExceptionUtils.getStackTrace(e)
        log.error(s"Unable to send mail! \n ${msg}")
        if (!silent) {
          throw new RuntimeException(s"Unable to send mail! \n ${msg}")
        }
    }
  }

  def sendMail(m: Mail) {
    settingService.findMailSettings match {
      case Some(s) => sendMail(m, s)
      case _ =>
    }
  }


}