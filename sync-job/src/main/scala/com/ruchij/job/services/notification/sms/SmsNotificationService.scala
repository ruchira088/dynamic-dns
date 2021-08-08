package com.ruchij.job.services.notification.sms

import com.ruchij.job.services.notification.NotificationService
import com.ruchij.job.services.notification.models.NotificationChannel
import com.ruchij.job.services.notification.sms.SmsNotificationService.Sms

trait SmsNotificationService[F[_]] extends NotificationService[F, Sms.type]

object SmsNotificationService {
  case object Sms extends NotificationChannel {
    override type DestinationID = PhoneNumber
    override type Message = String
  }

  class PhoneNumber private (val value: String) extends AnyVal

}
