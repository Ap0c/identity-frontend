package com.gu.identity.frontend.services

import com.amazonaws.regions.{Region, Regions}
import com.gu.identity.frontend.logging.Logging
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{AmazonS3Exception, GetObjectRequest, S3Object}
import com.gu.identity.frontend.configuration.Configuration.AWSConfig

import scala.io.{Codec, Source}

trait S3 extends Logging {

  private lazy val region = Region.getRegion(Regions.fromName(AWSConfig.region.getName))

  lazy val client = new AmazonS3Client(AWSConfig.credentials)
  client.setEndpoint(region.getServiceEndpoint("s3"))

  private def withS3Result[T](bucket: String, key: String)(action: S3Object => T): Option[T] = {
    logger.info(s"S3 getting bucket: $bucket Key: $key")
    try {
       val request = new GetObjectRequest(bucket, key)
       val result = client.getObject(request)
       logger.info(s"S3 got ${result.getObjectMetadata.getContentLength} bytes from ${result.getKey}")

       try {
         Some(action(result))
       }
       catch {
         case ex: Exception => throw ex
       }
       finally { result.close() }
     } catch {
       case ex: AmazonS3Exception if ex.getStatusCode == 404 => {
         logger.warn(s"Not found at $bucket  - $key")
         None
       }
       case ex: Exception => throw ex
     }
  }

  def get(bucket: String, key: String)(implicit codec: Codec): Option[String] = withS3Result(bucket, key) {
      result => Source.fromInputStream(result.getObjectContent).mkString
  }
}

object S3InfoSec extends S3 {
  def getBlockedEmailDomains(bucket: String, key: String) = get(bucket, key)
}
