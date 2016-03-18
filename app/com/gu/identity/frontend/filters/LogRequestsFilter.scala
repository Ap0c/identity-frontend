package com.gu.identity.frontend.filters

import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object LogRequestsFilter extends Filter {

  private val logger = Logger(this.getClass)

  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val start = System.nanoTime

    def logTime(result: Result) {
      if(!rh.path.contains("/management/healthcheck")) {
        val time = (System.nanoTime - start) / 1000000.0
        val activity = s"${rh.method} ${rh.uri}"
        logger.info(s"$activity completed in $time ms with status ${result.header.status}")
      }
    }

    val result = f(rh)

    result.foreach(logTime)

    result
  }
}
