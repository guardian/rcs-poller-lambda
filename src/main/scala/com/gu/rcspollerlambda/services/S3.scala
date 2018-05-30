package com.gu.rcspollerlambda.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }

object S3 {
  def getS3Client(credentialsProviderChain: AWSCredentialsProviderChain): AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProviderChain).build()
}
