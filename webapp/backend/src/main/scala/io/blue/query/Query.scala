package io.blue.query

import java.io.File
import org.apache.commons.io.FileUtils
import org.springframework.util.ResourceUtils


object Query {

  def allSessions = {
    val url = getClass.getClassLoader.getResource("classpath:query/session/all-sessions.sql")
    FileUtils.readFileToString(new File(url.getFile))
  }

  def blockingSessions = {
    val url = getClass.getClassLoader.getResource("classpath:query/session/blocking-sessions.sql")
    FileUtils.readFileToString(new File(url.getFile))
  }

}