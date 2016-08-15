package com.gu.identity.frontend.configuration

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions._
import play.api.{Configuration => PlayConfiguration}


case class Configuration(
  identityCookieDomain: String,

  identityApiKey: String,
  identityApiHost: String,

  identityProfileBaseUrl: String,

  dotcomBaseUrl: String,
  membershipBaseUrl: String,

  identityFederationApiHost: String,

  omnitureAccount: String,

  recaptchaEnabled: Boolean,
  googleRecaptchaSiteKey: String,
  googleRecaptchaSecretKey: String,

  identityCookiePublicKey: String,

  // endpoint for reporting errors to Sentry
  sentryDsnJs: String,
  sentryDsnScala: String,

  //S£ bucket details for infosex

  infoSecS3BucketName: String,
  infoSecS3BucketKey: String,

  underlying: PlayConfiguration)


object Configuration {
  def apply(appConfiguration: PlayConfiguration): Configuration = {

    def getString(path: String) =
      appConfiguration.getString(path).getOrElse(sys.error(s"Missing configuration: $path"))

    def getBoolean(path: String) =
      appConfiguration.getBoolean(path).getOrElse(sys.error(s"Missing configuration: $path"))


    Configuration(
      identityCookieDomain = getString("identity.frontend.cookieDomain"),

      identityApiKey = getString("identity.api.key"),
      identityApiHost = getString("identity.api.host"),

      identityFederationApiHost = getString("identity.federation.api.host"),

      identityProfileBaseUrl = getString("identity.frontend.baseUrl"),

      dotcomBaseUrl = getString("identity.frontend.dotcomBaseUrl"),

      membershipBaseUrl = getString("identity.frontend.membershipBaseUrl"),

      omnitureAccount = getString("omniture.account"),

      googleRecaptchaSiteKey = getString("google.recaptcha.site"),

      googleRecaptchaSecretKey = getString("google.recaptcha.secret"),

      recaptchaEnabled = getBoolean("google.recaptchaEnabled"),

      identityCookiePublicKey = getString("identityCookie.publicKey"),

      sentryDsnJs = getString("sentry.dsn.js"),

      sentryDsnScala = getString("sentry.dsn.scala"),

      infoSecS3BucketName = getString("infosec.s3.bucket.name"),

      infoSecS3BucketKey = getString("infosec.s3.bucket.key"),

      underlying = appConfiguration
    )
  }


  lazy val testConfiguration = Configuration(
    identityCookieDomain = "dev-theguardian.com",

    identityApiKey = "--test-key--",
    identityApiHost = "idapi.code.dev-theguardian.com",

    identityFederationApiHost = "https://oauth.code.dev-theguardian.com",

    identityProfileBaseUrl = "https://profile.code.dev-theguardian.com",

    dotcomBaseUrl = "http://www.theguardian.com",
    membershipBaseUrl = "https://members.theguardian.com",

    omnitureAccount = "--test-omniture-account--",

    googleRecaptchaSiteKey = "--recaptcha-key--",

    googleRecaptchaSecretKey = "--recaptcha-secret-key--",

    recaptchaEnabled = true,

    identityCookiePublicKey = "",

    sentryDsnJs = "--stubbed-sentry-dsn-js--",

    sentryDsnScala = "--stubbed-sentry-dsn-scala--",

    infoSecS3BucketName = "infosec.s3.bucket.name",

    infoSecS3BucketKey = "infosec.s3.bucket.key",

    underlying = PlayConfiguration.empty
  )

  object AWSConfig {
    val credentials: AWSCredentialsProvider = {
      val provider = new AWSCredentialsProviderChain(
        new EnvironmentVariableCredentialsProvider(),
        new SystemPropertiesCredentialsProvider(),
        new ProfileCredentialsProvider(),
        new InstanceProfileCredentialsProvider
      )
      provider
    }

    val clientConfiguration: ClientConfiguration = new ClientConfiguration()

    val region: Region = {
      val defaultRegion: Region = Region.getRegion(EU_WEST_1)
      Option(getCurrentRegion()).getOrElse(defaultRegion)
    }

  }

  object Environment {
    lazy val stage = System.getProperty("stage", "DEV")
  }
}
