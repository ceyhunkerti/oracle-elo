package io.blue.config.auth


import java.util.Arrays
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import scala.collection.JavaConversions._

import io.blue.service.UserService

@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  @Value("${security.jwt.client-id}")
  private var clientId: String = _

  @Value("${security.jwt.client-secret}")
  private var clientSecret: String = _

  @Value("${security.jwt.grant-type}")
  private var grantType: String = _

  @Value("${security.jwt.scope-read}")
  private var scopeRead: String = _

  @Value("${security.jwt.scope-write}")
  private var scopeWrite: String = "write"

  @Value("${security.jwt.resource-ids}")
  private var resourceIds: String = _

  @Autowired
  private var tokenStore: TokenStore = _

  @Autowired
  private var accessTokenConverter: JwtAccessTokenConverter = _

  @Autowired
  private var authenticationManager: AuthenticationManager = _

  override def configure(configurer: ClientDetailsServiceConfigurer): Unit = {
    configurer
      .inMemory
      .withClient(clientId)
      .secret(clientSecret)
      .authorizedGrantTypes(grantType)
      .scopes(scopeRead, scopeWrite)
      .resourceIds(resourceIds)
  }

  override def configure(endpoints: AuthorizationServerEndpointsConfigurer): Unit = {
    val enhancerChain: TokenEnhancerChain = new TokenEnhancerChain()
    enhancerChain.setTokenEnhancers(List(accessTokenConverter))
    endpoints
      .tokenStore(tokenStore)
      .accessTokenConverter(accessTokenConverter)
      .tokenEnhancer(enhancerChain)
      .authenticationManager(authenticationManager)
  }
}
