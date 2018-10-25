package io.blue.model.meta

import scala.beans.BeanProperty

class MetaTable {

  @BeanProperty
  var dbLink: String = _

  @BeanProperty
  var sourceSchema: String = _

  @BeanProperty
  var sourceTable: String = _

  @BeanProperty
  var owner: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var options: String = _

  @BeanProperty
  var columns: List[String] = List()

}