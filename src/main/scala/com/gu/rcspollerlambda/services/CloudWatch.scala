package com.gu.rcspollerlambda.services

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.cloudwatch.model.{ MetricDatum, PutMetricDataRequest, PutMetricDataResult }
import com.gu.rcspollerlambda.config.Config

import scala.util.Try

object CloudWatch extends Logging with Config {
  private val metricName = "RCSPollerErrors"
  private val namespace = "rcs-poller-alarms"

  object LoggingAsyncHandler extends AsyncHandler[PutMetricDataRequest, PutMetricDataResult] {
    def onError(exception: Exception) =
      logger.warn(s"CloudWatch PutMetricDataRequest error: ${exception.getMessage}}")

    def onSuccess(request: PutMetricDataRequest, result: PutMetricDataResult) =
      logger.info("CloudWatch PutMetricDataRequest - success")
  }

  def publishError = {
    logger.info(s"Sending error to CloudWatch...")
    val metric = new MetricDatum()
      .withValue(1D)
      .withMetricName(metricName)

    val request = new PutMetricDataRequest()
      .withNamespace(namespace)
      .withMetricData(metric)

    Try(AWS.cloudwatchClient.putMetricDataAsync(request, LoggingAsyncHandler)).recover {
      case error =>
        logger.warn(s"Failed to send CloudWatch metric data: ${error.getMessage}", error)
    }
  }
}