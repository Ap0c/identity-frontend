package com.gu.identity.frontend.models

import play.api.mvc.Call

object UrlBuilder {

  private def encode = java.net.URLEncoder.encode(_: String, "UTF8")

  def apply(baseUrl: String, params: Seq[(String, String)]): String =
    params.headOption match {
      case None => baseUrl
      case _ => {
        val paramString = params.map {
          case (key, value) => s"$key=${encode(value)}"
        }.mkString("&")

        s"$baseUrl?$paramString"
      }
    }

  def apply(call: Call, params: Seq[(String, String)]): String =
    apply(call.url, params)


  def apply(call: Call, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean]): String =
    apply(call.url, returnUrl, skipConfirmation)


  def apply(baseUrl: String, returnUrl: ReturnUrl, skipConfirmation: Option[Boolean]): String = {
    val params = Seq(skipConfirmation.map("skipConfirmation" -> _.toString)).flatten

    if (isDefaultReturnUrl(returnUrl)) {
      apply(baseUrl, params)
    } else {
      apply(baseUrl, params ++ Seq("returnUrl" -> returnUrl.url))
    }
  }

  private def isDefaultReturnUrl(returnUrl: ReturnUrl): Boolean = {
    returnUrl.url == "http://www.theguardian.com"
  }
}
