package com.gu.identity.frontend.jobs

import akka.agent.Agent
import com.gu.identity.frontend.logging.Logging
import com.gu.identity.frontend.services.S3InfoSec
import com.gu.identity.frontend.utils.ExecutionContexts

object BlockedEmailDomainList extends ExecutionContexts with Runnable with Logging {

  private val blockedDomainsAgent = Agent[Set[String]](Set.empty)

  def run() {

    logger.info("Getting list of blocked domains");

    val t = Set("one","two","three")
    val domains = S3InfoSec.getBlockedEmailDomains map {
      blockedDomains => blockedDomains.split("\n").toSet
    } getOrElse Set()
    blockedDomainsAgent.send(domains)
  }

  def getBlockedDomains = blockedDomainsAgent.get()

}
