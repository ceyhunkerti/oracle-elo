package io.blue.config

import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.context.annotation.{Bean, Configuration, ComponentScan}

@Configuration
@EnableAsync
@ComponentScan(Array("io.blue.util"))
class SpringAsyncConfig {

}
