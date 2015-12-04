package test.pages

import test.util.{WebBrowserUtil, Config}
import org.scalatest.selenium.{Page, WebBrowser}

class RegisterConfirm extends Page with WebBrowser with WebBrowserUtil {
  val url = s"""${Config.baseUrl}/register/confirm?returnUrl=${Config.baseUrl}/register"""

  def confirmRegistration() = {
    assert(pageHasElement(confirmRegistrationButton))
    click.on(confirmRegistrationButton)
  }

  def pageHasLoaded(): Boolean = {
    pageHasElement(confirmRegistrationButton)
  }

  private lazy val confirmRegistrationButton = className(s"submit-input")
}
