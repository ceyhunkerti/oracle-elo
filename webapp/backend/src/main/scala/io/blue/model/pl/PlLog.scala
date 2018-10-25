package io.blue.model.pl

import scala.beans.BeanProperty

class PlLog {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var level: String = _

  @BeanProperty
  var startTime: java.sql.Timestamp = _

  @BeanProperty
  var endTime: java.sql.Timestamp = _

  @BeanProperty
  var message: String = _

  @BeanProperty
  var statement: String = _
}