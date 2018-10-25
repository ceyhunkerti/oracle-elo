package io.blue.controller

import java.util.Date
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletResponse

import org.springframework.security.access.annotation.Secured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation._
import org.springframework.dao.DataIntegrityViolationException

import io.blue.service.ConnectionService
import io.blue.exception.UniqueConstraintViolationException

@RestController
@RequestMapping(Array("/api/v1/connections"))
class ConnectionController  @Autowired()(private val connectionService: ConnectionService) {

}