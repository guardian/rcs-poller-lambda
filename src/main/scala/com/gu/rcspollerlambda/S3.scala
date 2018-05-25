package com.gu.rcspollerlambda

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3Client

object S3 {
  def getS3Client(credentialsProviderChain: AWSCredentialsProviderChain): AmazonS3Client =
    new AmazonS3Client(credentialsProviderChain)
}
