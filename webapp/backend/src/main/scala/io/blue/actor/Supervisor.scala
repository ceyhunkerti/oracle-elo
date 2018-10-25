package io.blue.actor

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Actor
import akka.routing.Router
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import akka.actor.{Props, ActorRef}

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

import io.blue.ext.SpringExtension
import io.blue.actor.message._
import io.blue.model._

@Component(value="supervisor")
@Scope("prototype")
class Supervisor extends Actor {

  private val logger: Logger  = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  @Autowired
  private var springExtension: SpringExtension = _

  private var mailActor: ActorRef = _

  override def preStart: Unit = {
    mailActor = context.system.actorOf(springExtension.props("mail"),"mail")
    hearthBeat
  }

  override def postStop: Unit = {
  }

  def receive = {
    case Tick => tick
    case _ => logger.warn("Opps ?")
  }

  def tick = {
    logger.info("Received Tick")
  }

  def hearthBeat = {
    logger.info("Start hearth beat Supervisor")
    val cancellable =
      context.system.scheduler.schedule(0 milliseconds, 15000 milliseconds,self,Tick)
  }

}