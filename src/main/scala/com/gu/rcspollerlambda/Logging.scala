package com.gu.rcspollerlambda

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{ Logger => LogbackLogger }
import ch.qos.logback.core.{ Appender, LayoutBase }
import com.gu.logback.appender.kinesis.KinesisAppender
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.{ LoggerFactory, Logger => SLFLogger }

trait Logging extends Config {
  lazy val logger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def configure {
    logger.info("bootstrapping kinesis appender if configured correctly")

    val stack = "composer"
    val appName = "rcs-poller-lambda"
    val elkKinesisStream = loggingStreamName

    logger.info(s"bootstrapping kinesis appender with $stack -> $appName -> $stage -> $elkKinesisStream")
    val context = logger.getLoggerContext

    val layout = new LogstashLayout()
    layout.setContext(context)
    layout.setCustomFields(s"""{"stack":"$stack","app":"$appName","stage":"$stage"}""")
    layout.start()

    val appender = new KinesisAppender()
    appender.setBufferSize(1000)
    appender.setRegion(awsRegion.getName)
    appender.setStreamName(elkKinesisStream)
    appender.setContext(context)
    appender.setLayout(layout.asInstanceOf[LayoutBase[Nothing]])
    appender.setCredentialsProvider(awsCredentials)

    appender.start()

    logger.addAppender(appender.asInstanceOf[Appender[ILoggingEvent]])
    logger.info("Configured kinesis appender")
  }

  if (elkLoggingEnabled) {
    configure
  }
}
