package com.ruchij.job.config

import com.ruchij.job.services.notification.sms.SmsNotificationService.PhoneNumber
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

object JobConfigReaders {
  implicit val phoneNumberConfigReader: ConfigReader[PhoneNumber] =
    ConfigReader.fromNonEmptyString {
      input => PhoneNumber.from(input).left.map {
        throwable => CannotConvert(input, classOf[PhoneNumber].getSimpleName, throwable.getMessage)
      }
    }
}
