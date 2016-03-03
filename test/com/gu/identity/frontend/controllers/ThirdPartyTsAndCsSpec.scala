package com.gu.identity.frontend.controllers

import akka.util.Timeout
import com.gu.identity.cookie.{IdentityKeys, IdentityCookieDecoder}
import com.gu.identity.frontend.authentication.CookieName
import com.gu.identity.frontend.configuration.Configuration
import com.gu.identity.frontend.errors.ErrorHandler
import com.gu.identity.frontend.models.GroupCode
import com.gu.identity.frontend.services.{ServiceGatewayError, IdentityService}
import com.gu.identity.service.client.AssignGroupResponse
import com.gu.identity.service.client.models.{UserGroup, User}
import org.mockito.Matchers.{any => argAny}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.mvc.{RequestHeader, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.duration._
import play.api.mvc.Results._

import scala.concurrent.{Await, ExecutionContext, Future}

class ThirdPartyTsAndCsSpec extends PlaySpec with MockitoSugar{

  trait WithControllerMockedDependencies {
    val mockIdentityService = mock[IdentityService]
    val testKeys = new IdentityKeys(Configuration.testConfiguration.identityCookiePublicKey)

    val cookieDecoder = new IdentityCookieDecoder(testKeys)
    val mockMessages = mock[MessagesApi]
    val testConfig = Configuration.testConfiguration
    val mockErrorHandler = mock[ErrorHandler]

    implicit lazy val executionContext: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

    val thirdPartyTsAndCsController = new ThirdPartyTsAndCs(mockIdentityService, cookieDecoder, testConfig, mockMessages, mockErrorHandler)
  }

  def createCookie(value: String, name: String) = {
    Cookie(name = name, value = value)
  }


  "Is user in group" should {
    "return true if user is in group specified" in new WithControllerMockedDependencies {
      val groupCode = "ABC"
      val userGroup = UserGroup(groupCode, "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      thirdPartyTsAndCsController.isUserInGroup(user, groupCode) mustEqual true
    }

    "return false if user is not in the group specified" in  new WithControllerMockedDependencies {
      val groupCode = "123"
      val userGroup = UserGroup("ABC", "Group/ABC")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      thirdPartyTsAndCsController.isUserInGroup(user, groupCode) mustEqual false
    }
  }

  "checkUserForGroupMembership" should {
    "return a future of true when the user is in already in the group specified" in new WithControllerMockedDependencies {
      val groupCode = "GRS"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup(groupCode, "Group/GRS")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Right(true)
    }

    "return a future of false when the user is not in the group specified" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup("123", "Group/GTNF")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Right(user)
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Right(false)
    }

    "return a future of sequence of service errors if it was not possible to check if the user is in the group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val group = GroupCode(groupCode).get
      val userGroup = UserGroup(groupCode, "Group/GTNF")
      val userGroups = List(userGroup)
      val user = User(userGroups = userGroups)

      val timeout = Timeout(5 seconds)

      val cookie = Cookie("Name", "Value")

      when(mockIdentityService.getUser(argAny[Cookie])(argAny[ExecutionContext]))
        .thenReturn {
          Future.successful{
            Left(Seq(ServiceGatewayError("Unexpected 500 error")))
          }
        }

      val future = thirdPartyTsAndCsController.checkUserForGroupMembership(group, cookie)
      val result = Await.result(future, timeout.duration)

      result mustEqual Left(Seq(ServiceGatewayError("Unexpected 500 error")))
    }
  }

  "GET /agree/:group" should {



  }

  "POST action/agree" should {
    "return a result of the return url when user has successfully been added to the group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "http://www.theguardian.com/sport"
      val validRequestCookieData = "WyIxMDAwMDgxMSIsMTQ2Mjg5MjgyNDYxMV0.MCwCFG_PdoPk2PpSO5KoXbRLWJ0BvuqhAhRFIt1mlDcO2SN1Y6X7ktSs_oRJJw"
      val cookie = createCookie(validRequestCookieData, CookieName.SC_GU_U.toString)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Right(AssignGroupResponse(groupCode))
          }
        }

      val fakeRequest = FakeRequest("POST", "/actions/GTNF")
        .withFormUrlEncodedBody("groupCode" -> groupCode, "returnUrl" -> returnUrl)
        .withCookies(cookie)

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      redirectLocation(result) mustEqual Some(returnUrl)
      status(result) mustEqual SEE_OTHER
    }

    "return bad request when it is not possible to add the user to group" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "http://www.theguardian.com/sport"
      val validRequestCookieData = "WyIxMDAwMDgxMSIsMTQ2Mjg5MjgyNDYxMV0.MCwCFG_PdoPk2PpSO5KoXbRLWJ0BvuqhAhRFIt1mlDcO2SN1Y6X7ktSs_oRJJw"
      val cookie = createCookie(validRequestCookieData, CookieName.SC_GU_U.toString)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Left(Seq(ServiceGatewayError("error")))
          }
        }

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(BadRequest("Fail")))

      val fakeRequest = FakeRequest("POST", "/actions/GTNF")
        .withFormUrlEncodedBody("groupCode" -> groupCode, "returnUrl" -> returnUrl)
        .withCookies(cookie)

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual BAD_REQUEST
    }

    "return bad request when the form submission to add to group action is invalid" in new WithControllerMockedDependencies {
      val groupCode = "GTNF"
      val returnUrl = "http://www.theguardian.com/sport"
      val validRequestCookieData = "WyIxMDAwMDgxMSIsMTQ2Mjg5MjgyNDYxMV0.MCwCFG_PdoPk2PpSO5KoXbRLWJ0BvuqhAhRFIt1mlDcO2SN1Y6X7ktSs_oRJJw"
      val cookie = createCookie(validRequestCookieData, CookieName.SC_GU_U.toString)

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(BadRequest("Fail")))

      val fakeRequest = FakeRequest("POST", "/actions/GTNF")
        .withFormUrlEncodedBody("group" -> groupCode, "returnUrl" -> returnUrl)
        .withCookies(cookie)

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual BAD_REQUEST
    }

    "return a not found error if group code is invalid" in new WithControllerMockedDependencies {
      val groupCode = "ABC"
      val returnUrl = "http://www.theguardian.com/sport"
      val validRequestCookieData = "WyIxMDAwMDgxMSIsMTQ2Mjg5MjgyNDYxMV0.MCwCFG_PdoPk2PpSO5KoXbRLWJ0BvuqhAhRFIt1mlDcO2SN1Y6X7ktSs_oRJJw"
      val cookie = createCookie(validRequestCookieData, CookieName.SC_GU_U.toString)

      when(mockIdentityService.assignGroupCode(groupCode, cookie))
        .thenReturn{
          Future.successful{
            Left(Seq(ServiceGatewayError("error")))
          }
        }

      when(mockErrorHandler.onClientError(argAny[RequestHeader], argAny[Int], argAny[String]))
        .thenReturn(Future.successful(NotFound("Fail")))

      val fakeRequest = FakeRequest("POST", "/actions/GTNF")
        .withFormUrlEncodedBody("groupCode" -> groupCode, "returnUrl" -> returnUrl)
        .withCookies(cookie)

      val result = call(thirdPartyTsAndCsController.addToGroupAction(), fakeRequest)
      status(result) mustEqual NOT_FOUND
    }
  }
}
