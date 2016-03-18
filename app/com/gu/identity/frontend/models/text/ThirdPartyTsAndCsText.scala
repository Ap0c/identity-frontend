package com.gu.identity.frontend.models.text

import com.gu.identity.frontend.models.{GuardianJobs, GuardianTeachersNetwork, GroupCode, ReturnUrl}
import play.api.i18n.Messages

case class BaseTsAndCsText private(
    title: String,
    explanationText: String,
    continueButtonText: String,
    termsText: String,
    termsOfServiceLinkText: String,
    privacyPolicyLinkText: String)

object BaseTsAndCsText {
  def apply(title: String, serviceName: String)(implicit messages: Messages): BaseTsAndCsText = {
    BaseTsAndCsText(
      title = messages("thirdPartyTerms.title", title),
      explanationText = messages("thirdPartyTerms.explanation"),
      continueButtonText = messages("thirdPartyTerms.continueButton"),
      termsText = messages("thirdPartyTerms.terms", serviceName),
      termsOfServiceLinkText = messages("thirdPartyTerms.termsOfService"),
      privacyPolicyLinkText = messages("thirdPartyTerms.privacyPolicy")
    )
  }
}

case class ThirdPartyTsAndCsText (
    pageTitle: String,
    title: String,
    features: Seq[String],
    serviceName: String,
    termsOfServiceLink: String,
    privacyPolicyLink: String,
    baseText: BaseTsAndCsText)

object TeachersTsAndCsText {
  def apply()(implicit messages: Messages): ThirdPartyTsAndCsText = {
    val title = messages("thirdPartyTerms.teachersTitle").replace(' ', '\u00A0')
    val serviceName = messages("thirdPartyTerms.teachersServiceName")
    ThirdPartyTsAndCsText(
      pageTitle = messages("thirdPartyTerms.teachersPageTitle"),
      title = title,
      features = Seq(messages("thirdPartyTerms.teachersFeatures")),
      serviceName = serviceName,
      termsOfServiceLink = "http://teachers.theguardian.com/Terms.htm",
      privacyPolicyLink = "http://teachers.theguardian.com/privacypolicy.htm",
      baseText = BaseTsAndCsText(title, serviceName)
    )
  }
}

object JobsTsAndCsText {
  def apply()(implicit messages: Messages): ThirdPartyTsAndCsText = {
    val title = messages("thirdPartyTerms.jobsTitle")
    val serviceName = messages("thirdPartyTerms.jobsServiceName")
    ThirdPartyTsAndCsText(
      pageTitle = messages("thirdPartyTerms.jobsPageTitle"),
      title = title,
      features = Seq(
        messages("thirdPartyTerms.jobsFeature1"),
        messages("thirdPartyTerms.jobsFeature2"),
        messages("thirdPartyTerms.jobsFeature3")),
      serviceName = serviceName,
      termsOfServiceLink = "https://jobs.theguardian.com/terms-and-conditions/",
      privacyPolicyLink = "https://jobs.theguardian.com/privacy-policy/",
      baseText = BaseTsAndCsText(title, serviceName)
    )
  }
}

object TsAndCsPageText {
  def getPageText(group: GroupCode)(implicit messages: Messages): ThirdPartyTsAndCsText = {
    group match {
      case GuardianTeachersNetwork => TeachersTsAndCsText()
      case GuardianJobs => JobsTsAndCsText()
    }
  }
}
