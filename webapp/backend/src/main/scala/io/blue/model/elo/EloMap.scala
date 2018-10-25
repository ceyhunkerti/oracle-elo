package io.blue.model.elo

import scala.beans.BeanProperty

class EloMap {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var dbLink: String = _

  @BeanProperty
  var sourceSchema: String = _

  @BeanProperty
  var sourceTable: String = _

  @BeanProperty
  var targetSchema: String = _

  @BeanProperty
  var targetTable: String = _

  @BeanProperty
  var filter: String = _

  @BeanProperty
  var sourceHint: String = _

  @BeanProperty
  var targetHint: String = _

  @BeanProperty
  var dropCreate: Boolean = _

  @BeanProperty
  var deltaColumn: String = _

  @BeanProperty
  var lastDelta: String = _

  @BeanProperty
  var excluded: Boolean = _

  @BeanProperty
  var analyze: Boolean = _

  @BeanProperty
  var status: String = _

  @BeanProperty
  var columns: List[EloColumn] = List()

}