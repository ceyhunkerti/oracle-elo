package io.blue.model.elo

import scala.beans.BeanProperty

class EloColumn {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var target: String = _

  @BeanProperty
  var expression: String = _

  @BeanProperty
  var excluded: Boolean = _

  // @BeanProperty
  // var mapped: Boolean = true

  // if mapped column does not exist in target table
  // then valid is false
  // var valid: Boolean = true
}