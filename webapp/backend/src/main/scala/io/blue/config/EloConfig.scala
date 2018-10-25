package io.blue.config

import org.springframework.context.annotation.{PropertySource, Bean, Configuration}
import org.springframework.beans.factory.annotation._

@Configuration
@PropertySource(Array("classpath:application.properties"))
class EloConfig {

  @Value( "${app.elo.db.url}" )
  var url: String = _

  @Value( "${app.elo.db.username}" )
  var username: String = _

  @Value( "${app.elo.db.password}" )
  var password: String = _

}