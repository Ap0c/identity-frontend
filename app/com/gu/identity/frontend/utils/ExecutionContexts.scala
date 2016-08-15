package com.gu.identity.frontend.utils

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import play.api.libs.concurrent.{Akka => PlayAkka}
import play.api.Play

trait ExecutionContexts {

  implicit lazy val executionContext: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
  lazy val actorSystem: ActorSystem = PlayAkka.system(Play.current)

}
