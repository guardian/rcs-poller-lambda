package com.gu.rcspollerlambda.services

import com.gu.rcspollerlambda.config.Config._
import software.amazon.awssdk.services.cloudwatch.model._

import scala.util.Try

object CloudWatch extends Logging {
  private val metricName = "RCSPollerErrors"
  private val namespace = "rcs-poller-alarms"

  def publishError = {
    logger.info(s"Sending error to CloudWatch...")
    publish(1d)
  }

  def publishOK = {
    logger.info(s"Sending ok to CloudWatch...")
    publish(0d)
  }

  private def publish(value: Double) = {
    val metric = MetricDatum
      .builder()
      .metricName(metricName)
      .value(value)
      .build()

    val request = PutMetricDataRequest
      .builder()
      .namespace(namespace)
      .metricData(metric)
      .build()

    Try(AWS.cloudwatchClient.putMetricData(request)).recover { case error =>
      logger.warn(
        s"Failed to send CloudWatch metric data: ${error.getMessage}",
        error
      )
    }
  }
}
