package io.blue.config.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.encoding.ShaPasswordEncoder
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import scala.collection.JavaConversions._

import io.blue.service.UserService

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${security.signing-key}")
  private var signingKey: String = _

  @Value("${security.encoding-strength}")
  private var encodingStrength: java.lang.Integer = _

  @Value("${security.security-realm}")
  private var securityRealm: String = _

  @Autowired
  private var userService: UserService = _

  @Bean
  protected override def authenticationManager(): AuthenticationManager =
    super.authenticationManager()

  protected override def configure(auth: AuthenticationManagerBuilder): Unit = {
    auth
      .userDetailsService(userService)
      .passwordEncoder(new ShaPasswordEncoder(encodingStrength))
  }

  protected override def configure(http: HttpSecurity): Unit = {
    http
      .sessionManagement
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and
      .httpBasic
      .realmName(securityRealm)
      .and
      .csrf
      .disable
      // .authorizerequests
      // .anyRequest
      // .fullyAuthenticated

    http.authorizeRequests.antMatchers("/api/**").fullyAuthenticated

    http
      .authorizeRequests
      .antMatchers("/index.html", "/", "/home", "/login","/favicon.ico","/*.js","/*.js.map").permitAll
  }

  @Bean
  def accessTokenConverter(): JwtAccessTokenConverter = {
    val converter: JwtAccessTokenConverter = new JwtAccessTokenConverter()
    converter.setSigningKey(signingKey)
    converter
  }

  @Bean
  def tokenStore(): TokenStore = new JwtTokenStore(accessTokenConverter())

  @Bean
  @Primary
  def tokenServices(): DefaultTokenServices = {
    val defaultTokenServices: DefaultTokenServices = new DefaultTokenServices()
    defaultTokenServices.setTokenStore(tokenStore())
    defaultTokenServices.setSupportRefreshToken(true)
    defaultTokenServices
  }
}

import org.springframework.security.oauth2.config.annotation.web.configuration._

@Configuration
@EnableResourceServer
class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

  override def configure( http: HttpSecurity) {
    http
      .antMatcher("/api/**")
      .authorizeRequests
      .antMatchers("/", "/login**", "/webjars/**", "/error**")
      .permitAll
      .anyRequest
      .authenticated
  }
}