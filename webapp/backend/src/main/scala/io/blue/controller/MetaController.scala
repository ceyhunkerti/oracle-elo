package io.blue.controller

import javax.servlet.http.HttpServletResponse
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation._
import org.springframework.dao.DataIntegrityViolationException
import org.apache.commons.io.IOUtils
import com.fasterxml.jackson.databind.ObjectMapper

import io.blue.model.meta._
import io.blue.service.MetaService

@RestController
@RequestMapping(Array("/api/v1/meta"))
class MetaController @Autowired()(private val service: MetaService) {

  @RequestMapping(value = Array("/create-table"), method = Array(RequestMethod.POST))
  def createTable(@RequestBody table: MetaTable) = service.createTable(table)


}