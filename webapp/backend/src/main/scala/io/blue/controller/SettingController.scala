package io.blue.controller

import java.util.Date
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletResponse

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation._

import io.blue.model.Setting
import io.blue.service.SettingService
import io.blue.exception.UniqueConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException

@RestController
@RequestMapping(Array("/api/v1/settings"))
class SettingController  @Autowired()(private val settingService: SettingService) {

  @RequestMapping(method = Array(RequestMethod.GET) )
  def findAll = settingService.findAll

  @RequestMapping(value = Array("/{id}"), method = Array(RequestMethod.GET))
  def findOne(@PathVariable("id") id: Long) = settingService.findOne(id)

  @RequestMapping(value = Array("/name/{name}"), method = Array(RequestMethod.GET))
  def findByName(@PathVariable("name") name: String) = settingService.findByName(name)

  @RequestMapping(value = Array("/active-mail-service"), method = Array(RequestMethod.GET))
  def activeMailService: Boolean = settingService.activeMailService

  @RequestMapping(method = Array(RequestMethod.POST))
  def save(@RequestBody setting: Setting) = settingService.save(setting)

  @RequestMapping(value = Array("/{id}"), method = Array(RequestMethod.PUT))
  def update(@PathVariable("id") id: Long, @RequestBody setting: Setting) =
    settingService.update(setting)

  @RequestMapping(value = Array("/{id}"), method = Array(RequestMethod.DELETE))
  def delete(@PathVariable("id") id: Long) = settingService.delete(id)

}