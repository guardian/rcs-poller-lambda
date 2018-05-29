package com.gu.rcspollerlambda

import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.{ AmazonSNS, AmazonSNSClientBuilder }
import io.circe.Json

object SNS extends Config {
  private lazy val client: AmazonSNS = AmazonSNSClientBuilder.standard()
    .withRegion(awsRegion)
    .withCredentials(awsCredentials).build()

  private val topicArn: String = "arn:aws:sns:eu-west-1:563563610310:media-service-DEV-Topic-5J6RZB9IFC38"

  def publish(message: Json) {
    val result = client.publish(new PublishRequest(topicArn, message.toString(), "update-rcs-rights"))
    println("Sent to SNS", result)
  }
}
