package com.gu.rcspollerlambda

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{ LoggerContext, Logger => LogbackLogger }
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.{ LoggerFactory, Logger => SLFLogger }

trait Logging {

  val logger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  (sys.env.get("Stack"), sys.env.get("App"), sys.env.get("Stage")) match {
    case (Some(stack), Some(app), Some(stage)) =>
      val newAppender = createAppender(stack, app, stage)

      disableExistingAppender()
      logger.addAppender(newAppender)

    case _ =>
    // leave logging alone
  }

  private def setLayout(context: LoggerContext, stack: String, app: String, stage: String): LogstashLayout = {
    val layout = new LogstashLayout()
    layout.setContext(context)
    layout.setCustomFields(s"""{"stack":"$stack","app":"$app","stage":"$stage"}""")
    layout.start()

    layout
  }

  private def createAppender(stack: String, app: String, stage: String) = {
    val layout = setLayout(logger.getLoggerContext, stack, app, stage)

    val encoder = new LayoutWrappingEncoder[ILoggingEvent]
    encoder.setLayout(layout)

    val appender = new ConsoleAppender[ILoggingEvent]
    appender.setEncoder(encoder)
    appender.start()

    appender
  }

  private def disableExistingAppender() = {
    Option(logger.getAppender("console")).foreach(_.stop())
  }
}
