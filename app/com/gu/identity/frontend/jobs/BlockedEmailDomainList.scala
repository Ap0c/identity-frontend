package com.gu.identity.frontend.jobs

import akka.actor.ActorSystem
import akka.agent.Agent
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.S3InfoSec
import com.gu.identity.frontend.utils.ExecutionContexts
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object BlockedEmailDomainList extends ExecutionContexts with Logging {

  private val blockedDomainsAgent = Agent[Set[String]](Set.empty)

  def run(configuration: Configuration): Unit = {

    logger.info("Getting list of blocked domains from" );

    val domains = S3InfoSec.getBlockedEmailDomains(configuration.infoSecS3BucketName,configuration.infoSecS3BucketKey) map {
      blockedDomains => blockedDomains.split("\n").toSet
    } getOrElse Set()
    blockedDomainsAgent.send(domains)
  }

  def isDomainBlocked(domain: String) = blockedDomainsAgent.get().contains(domain)

}

class BlockedEmailDomainListJob(theActorSystem: ActorSystem, configuration: Configuration) extends ExecutionContexts with Logging  {

  //Need to do this to avoid an "There is no started Application" error: We want the executionContext from the trait but not the actor syatem
  override lazy val actorSystem = theActorSystem

  def start = {
    logger.info("Starting job to update list of barred registration domains every half houe");

    actorSystem.scheduler.schedule(20.seconds, 30.minutes) {
      BlockedEmailDomainList.run(configuration)
    }
  }
}
