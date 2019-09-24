package com.gu.rcspollerlambda.services

import com.amazonaws.services.cloudwatch.model.{ MetricDatum, PutMetricDataRequest }
import com.gu.rcspollerlambda.config.Config._

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
    val metric = new MetricDatum()
      .withValue(value)
      .withMetricName(metricName)

    val request = new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(metric)

    Try(AWS.cloudwatchClient.putMetricData(request)).recover {
      case error =>
        logger.warn(s"Failed to send CloudWatch metric data: ${error.getMessage}", error)
    }
  }
}