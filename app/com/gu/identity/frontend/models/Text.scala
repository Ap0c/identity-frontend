package com.gu.identity.frontend.models

import play.api.i18n.Messages

object Text {
  object SignInPageText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map (
        "title" -> messages("signin.title"),
        "pageTitle" -> messages("signin.pagetitle"),
        "prelude" -> messages("signin.prelude"),
        "preludeMoreInfo" -> messages("signin.prelude.moreinfo"),
        "preludeFaq" -> messages("signin.prelude.faq"),
        "email" -> messages("signin.email"),
        "signInWithEmail" -> messages("signin.signInWithEmail"),
        "divideText" -> messages("signin.dividetext"),
        "password" -> messages("signin.password"),
        "forgottenPassword" -> messages("signin.forgottenpassword"),
        "rememberMe" -> messages("signin.rememberme"),
        "signIn" -> messages("signin.signin"),
        "noAccount" -> messages("signin.noaccount"),
        "signUp" -> messages("signin.signup"),
        "conditions" -> messages("signin.conditions"),
        "termsOfService" -> messages("signin.termsofservice"),
        "privacyPolicy" -> messages("signin.privacypolicy")
      )
    }
  }

  object LayoutText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map(
        "layoutPageTitle" -> messages("layout.pagetitle")
      )
    }
  }

  object HeaderText {
    def toMap(implicit messages: Messages): Map[String, String] = {
      Map(
        "back" -> messages("header.backtext"),
        "logo" -> messages("header.logo")
      )
    }
  }
}
