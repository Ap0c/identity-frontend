package com.gu.identity.frontend.views.models

import com.gu.identity.frontend.configuration.{MultiVariantTestVariant, MultiVariantTest, Configuration}
import com.gu.identity.frontend.controllers.routes
import com.gu.identity.frontend.csrf.CSRFToken
import com.gu.identity.frontend.models.{UrlBuilder, ReturnUrl}
import com.gu.identity.frontend.models.text.RegisterText
import play.api.i18n.Messages


case class RegisterViewModel(
    layout: LayoutViewModel,

    oauth: OAuthRegistrationViewModel,

    registerPageText: RegisterText,
    terms: TermsViewModel,

    hasErrors: Boolean,
    errors: Seq[ErrorViewModel],

    csrfToken: Option[CSRFToken],
    returnUrl: String,
    skipConfirmation: Boolean,

    actions: RegisterActions,
    links: RegisterLinks,

    resources: Seq[PageResource with Product],
    indirectResources: Seq[PageResource with Product])
  extends ViewModel with ViewModelResources


object RegisterViewModel {

  def apply(
      configuration: Configuration,
      activeTests: Iterable[(MultiVariantTest, MultiVariantTestVariant)],
      errors: Seq[ErrorViewModel],
      csrfToken: Option[CSRFToken],
      returnUrl: ReturnUrl,
      skipConfirmation: Option[Boolean],
      group: Option[String])
      (implicit messages: Messages): RegisterViewModel = {

    val layout = LayoutViewModel(configuration, activeTests)

    RegisterViewModel(
      layout = layout,

      oauth = OAuthRegistrationViewModel(configuration, returnUrl, skipConfirmation),

      registerPageText = RegisterText(),
      terms = Terms.getTermsModel(group),

      hasErrors = errors.nonEmpty,
      errors = errors,

      csrfToken = csrfToken,
      returnUrl = returnUrl.url,
      skipConfirmation = skipConfirmation.getOrElse(false),

      actions = RegisterActions(),
      links = RegisterLinks(returnUrl, skipConfirmation),

      resources = layout.resources,
      indirectResources = layout.indirectResources
    )
  }

}


case class RegisterActions private(
    register: String)

object RegisterActions {
  def apply(): RegisterActions =
    RegisterActions(
      register = routes.RegisterAction.register().url
    )
}


case class RegisterLinks private(
    signIn: String)

object RegisterLinks {
  def apply(returnUrl: ReturnUrl, skipConfirmation: Option[Boolean]): RegisterLinks =
    RegisterLinks(
      signIn = UrlBuilder(routes.Application.signIn().url, returnUrl, skipConfirmation)
    )
}
