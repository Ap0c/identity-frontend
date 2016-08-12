package com.gu.identity.frontend.request

import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors._
import com.gu.identity.frontend.jobs.BlockedEmailDomainList
import com.gu.identity.frontend.models.{ClientID, GroupCode, ReturnUrl}
import com.gu.identity.frontend.request.RequestParameters._
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Constraints, Invalid, Valid, ValidationError}
import play.api.data.{Form, FormError, Mapping}
import play.api.http.HeaderNames
import play.api.mvc.{BodyParser, BodyParsers, RequestHeader, Result}

import scala.util.matching.Regex


case class RegisterActionRequestBody private(
    firstName: String,
    lastName: String,
    email: String,
    username: String,
    password: String,
    receiveGnmMarketing: Boolean,
    receive3rdPartyMarketing: Boolean,
    returnUrl: Option[ReturnUrl],
    skipConfirmation: Option[Boolean],
    groupCode: Option[GroupCode],
    clientId: Option[ClientID],
    csrfToken: String)
  extends SignInRequestParameters
  with ReturnUrlRequestParameter
  with SkipConfirmationRequestParameter
  with ClientIdRequestParameter
  with GroupRequestParameter
  with CSRFTokenRequestParameter {

  // activate "rememberMe" on registrations
  val rememberMe = true
}

object RegisterActionRequestBody {

  lazy val bodyParser =
    FormRequestBodyParser("RegisterActionRequestBody")(registerForm)(handleFormErrors)

  def registerForm(requestHeader: RequestHeader): Form[RegisterActionRequestBody] =
    registerForm(requestHeader.headers.get(HeaderNames.REFERER))

  def registerForm(refererHeader: Option[String]): Form[RegisterActionRequestBody] =
    Form(FormMapping.registerFormMapping(refererHeader))


  private def handleFormErrors(formError: FormError): AppException = formError match {
    case FormError("csrfToken", _, _) => ForgeryTokenAppException("Missing csrfToken on request")
    case FormError("firstName", msg, _) => RegisterActionInvalidFirstNameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("lastName", msg, _) => RegisterActionInvalidLastNameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("email", msg, _) => RegisterActionInvalidEmailAppException(msg.headOption.getOrElse("unknown"))
    case FormError("username", msg, _) => RegisterActionInvalidUsernameAppException(msg.headOption.getOrElse("unknown"))
    case FormError("password", msg, _) => RegisterActionInvalidPasswordAppException(msg.headOption.getOrElse("unknown"))
    case FormError("groupCode", msg, _) => RegisterActionInvalidGroupAppException(msg.headOption.getOrElse("unknown"))
    case e => RegisterActionBadRequestAppException(s"Unexpected error: ${e.message}")
  }


  object FormMapping {
    import ClientID.FormMapping.clientId
    import GroupCode.FormMappings.groupCode
    import ReturnUrl.FormMapping.returnUrl


    private val EmailPattern = """(.*)@(.*)""".r

    def registrationEmailAddress: Constraint[String] = Constraint[String]("constraint.email") {
      email =>
        if (email.matches(EmailPattern.toString())) {
          val EmailPattern(name, domain) = email
          BlockedEmailDomainList.getBlockedDomains.contains(domain) match {
            case true => Invalid(ValidationError("error.email"))
            case false => Valid
          }
        } else {
          Valid
        }
    }

    private val registrationEmail: Mapping[String] = text.verifying(Constraints.emailAddress, registrationEmailAddress)


    private val username: Mapping[String] = text.verifying(
      "error.username", name => name.matches("[A-z0-9]+") && name.length > 5 && name.length < 21
    )

    private val password: Mapping[String] = text.verifying(
      "error.password", name => name.length > 5 && name.length < 73
    )


    def registerFormMapping(refererHeader: Option[String]): Mapping[RegisterActionRequestBody] =
      mapping(
        "firstName" -> nonEmptyText,
        "lastName" -> nonEmptyText,
        "email" -> registrationEmail,
        "username" -> username,
        "password" -> password,
        "receiveGnmMarketing" -> boolean,
        "receive3rdPartyMarketing" -> boolean,
        "returnUrl" -> returnUrl(refererHeader),
        "skipConfirmation" -> optional(boolean),
        "groupCode" -> optional(groupCode),
        "clientId" -> optional(clientId),
        "csrfToken" -> text
      )(RegisterActionRequestBody.apply)(RegisterActionRequestBody.unapply)
  }
}
