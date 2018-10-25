package io.blue.config.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices

import scala.collection.JavaConversions._

@Configuration
@EnableResourceServer
class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  @Autowired
  private var tokenServices: ResourceServerTokenServices = _

  @Value("${security.jwt.resource-ids}")
  private var resourceIds: String = _

  override def configure(resources: ResourceServerSecurityConfigurer): Unit = {
    resources.resourceId(resourceIds).tokenServices(tokenServices)
  }

}
