package com.gu.identity.frontend.services

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.controllers.RegisterRequest
import com.gu.identity.frontend.models.TrackingData
import com.gu.identity.service.client._
import org.joda.time.{DateTime, Seconds}
import play.api.mvc.{Cookie => PlayCookie}

import scala.concurrent.{ExecutionContext, Future}


/**
 * Adapter for the identity service client.
 */
trait IdentityService {
  def authenticate(email: Option[String], password: Option[String], rememberMe: Boolean, trackingData: TrackingData)(implicit ec: ExecutionContext): Future[Either[Seq[ServiceError], Seq[PlayCookie]]]
  def register(request: RegisterRequest)(implicit ec: ExecutionContext): Future[Either[Seq[ServiceError], Seq[PlayCookie]]]
}


class IdentityServiceImpl(config: Configuration, adapter: IdentityServiceRequestHandler) extends IdentityService {

  implicit val clientConfiguration = IdentityClientConfiguration(config.identityApiHost, config.identityApiKey, adapter)

  def authenticate(email: Option[String], password: Option[String], rememberMe: Boolean, trackingData: TrackingData)(implicit ec: ExecutionContext) = {
    IdentityClient.authenticateCookies(email, password, rememberMe, trackingData).map {
      case Left(errors) => Left {
        errors.map {
          case e: BadRequest => ServiceBadRequest(e.message, e.description)
          case e: GatewayError => ServiceGatewayError(e.message, e.description)
        }
      }
      case Right(cookies) => Right(cookies.map { c =>
        val maxAge = if (rememberMe) Some(Seconds.secondsBetween(DateTime.now, c.expires).getSeconds) else None
        val secureHttpOnly = c.key.startsWith("SC_")
        val cookieMaxAgeOpt = maxAge.filterNot(_ => c.isSession)

        PlayCookie(c.key, c.value, cookieMaxAgeOpt, "/", Some(config.identityCookieDomain), secure = secureHttpOnly, httpOnly = secureHttpOnly)
      })
    }
  }

  override def register(request: RegisterRequest)(implicit ec: ExecutionContext): Future[Either[Seq[ServiceError], Seq[PlayCookie]]] = {
    val apiRequest = RegisterApiRequest(request.email, request.password)
    IdentityClient.register(apiRequest).map {
      case Left(errors) => Left {
        errors.map {
          case e: BadRequest => ServiceBadRequest(e.message, e.description)
          case e: GatewayError => ServiceGatewayError(e.message, e.description)
        }
      }
      case Right(cookies) => Right(cookies.map { c =>
        val maxAge = if (false) Some(Seconds.secondsBetween(DateTime.now, c.expires).getSeconds) else None
        val secureHttpOnly = c.key.startsWith("SC_")
        val cookieMaxAgeOpt = maxAge.filterNot(_ => c.isSession)

        PlayCookie(c.key, c.value, cookieMaxAgeOpt, "/", Some(config.identityCookieDomain), secure = secureHttpOnly, httpOnly = secureHttpOnly)
      })
    }
  }
}
