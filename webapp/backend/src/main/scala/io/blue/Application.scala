
package io.blue


import akka.actor.ActorRef
import akka.actor.ActorSystem
import io.blue.ext.SpringExtension

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class AppConfig

object Application extends App {
  SpringApplication.run(classOf[AppConfig])
}


