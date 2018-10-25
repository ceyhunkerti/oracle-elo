package io.blue.config.auth


import scala.collection.JavaConversions._
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class AdditionalWebConfig {

  @Bean
  def corsFilter(): FilterRegistrationBean = {
    val source: UrlBasedCorsConfigurationSource =
      new UrlBasedCorsConfigurationSource()
    val config: CorsConfiguration = new CorsConfiguration()
    config.setAllowCredentials(true)
    config.addAllowedOrigin("*")
    config.addAllowedHeader("*")
    config.addAllowedMethod("*")
    source.registerCorsConfiguration("/**", config)
    val bean: FilterRegistrationBean = new FilterRegistrationBean(
      new CorsFilter(source))
    bean.setOrder(0)
    bean
  }
}

import javax.servlet.http._
import javax.servlet._
import org.springframework.core.annotation._

@Configuration
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
class SimpleCORSFilter extends Filter {

  override def init(fc: FilterConfig): Unit = {}

  override def doFilter(req: ServletRequest,
                        resp: ServletResponse,
                        chain: FilterChain): Unit = {
    val response: HttpServletResponse = resp.asInstanceOf[HttpServletResponse]
    val request: HttpServletRequest = req.asInstanceOf[HttpServletRequest]
    response.setHeader("Access-Control-Allow-Origin", "*")
    response.setHeader("Access-Control-Allow-Methods",
                       "POST, GET, OPTIONS, DELETE, PUT")
    response.setHeader("Access-Control-Max-Age", "3600")
    response.setHeader(
      "Access-Control-Allow-Headers",
      "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN")
    if ("OPTIONS".equalsIgnoreCase(request.getMethod)) {
      response.setStatus(HttpServletResponse.SC_OK)
    } else {
      chain.doFilter(req, resp)
    }
  }

  override def destroy(): Unit = {}

}
