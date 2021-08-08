package com.ruchij.job.config

import com.ruchij.job.services.notification.sms.SmsNotificationService.PhoneNumber

case class NotificationConfig(alertSmsPhoneNumber: Option[PhoneNumber])
