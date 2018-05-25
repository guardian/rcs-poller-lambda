package com.gu.rcspollerlambda

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }

object S3 extends Config {
  def getS3Client(credentialsProviderChain: AWSCredentialsProviderChain): AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(awsRegion).withCredentials(credentialsProviderChain).build()
}
