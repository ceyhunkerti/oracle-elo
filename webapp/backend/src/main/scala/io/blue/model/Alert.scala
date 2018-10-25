package io.blue.model

import com.fasterxml.jackson.annotation._
import scala.beans.BeanProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize


object AlertType {
}


class Alert {
  @BeanProperty
  var tp : String = _

  @BeanProperty
  var msg: String = _

  @BeanProperty
  var obj: AnyRef = _

}