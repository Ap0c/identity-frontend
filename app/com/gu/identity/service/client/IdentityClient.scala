package com.gu.identity.service.client

import scala.concurrent.{ExecutionContext, Future}

object IdentityClient extends Logging {

  def authenticateCookies(email: Option[String], password: Option[String], rememberMe: Boolean)(implicit configuration: IdentityClientConfiguration, ec: ExecutionContext): Future[Either[IdentityClientErrors, Seq[IdentityCookie]]] =
    AuthenticateCookiesRequest.from(email, password, rememberMe) match {
      case Right(request) => authenticateCookies(request)
      case Left(err) => Future.successful(Left(Seq(err)))
    }

  def authenticateCookies(request: AuthenticateCookiesRequest)(implicit configuration: IdentityClientConfiguration, ec: ExecutionContext): Future[Either[IdentityClientErrors, Seq[IdentityCookie]]] =
    configuration.requestHandler.handleRequest(request).map {
      case Left(error) => Left(error)
      case Right(AuthenticationCookiesResponse(cookies)) =>
        Right(cookies.values.map { c =>
          IdentityCookie(c.key, c.value, c.sessionCookie.getOrElse(false), cookies.expiresAt)
        })
      case Right(other) => Left(Seq(GatewayError("Unknown response")))
    }

}
