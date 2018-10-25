package io.blue.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
case class UnSupportedConnectionTypeException(message: String) extends RuntimeException(message) 