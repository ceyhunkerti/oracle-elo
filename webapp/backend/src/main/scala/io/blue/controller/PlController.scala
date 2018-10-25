package io.blue.controller

import javax.servlet.http.HttpServletResponse
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation._
import org.springframework.dao.DataIntegrityViolationException
import org.apache.commons.io.IOUtils
import com.fasterxml.jackson.databind.ObjectMapper

import io.blue.model.elo._
import io.blue.service.PlService

@RestController
@RequestMapping(Array("/api/v1/pl"))
class PlController @Autowired()(private val service: PlService) {

  @RequestMapping(value = Array("/logs"), method = Array(RequestMethod.GET))
  def findDbLinks(@RequestParam(name = "limit", required = false) limit: java.lang.Long ) = {
    if (limit == null) service.findLogs() else service.findLogs(limit)
  }

}