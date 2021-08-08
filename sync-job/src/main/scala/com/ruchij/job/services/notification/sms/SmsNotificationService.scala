package com.ruchij.job.services.notification.sms

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ruchij.job.services.notification.NotificationService
import com.ruchij.job.services.notification.models.NotificationChannel
import com.ruchij.job.services.notification.sms.SmsNotificationService.Sms

import scala.util.Try

trait SmsNotificationService[F[_]] extends NotificationService[F, Sms.type]

object SmsNotificationService {
  case object Sms extends NotificationChannel {
    override type DestinationID = PhoneNumber
    override type Message = String
  }

  class PhoneNumber private (val value: String) extends AnyVal

  object PhoneNumber {
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    def from(input: String): Either[Throwable, PhoneNumber] =
      Try(phoneNumberUtil.parse(input, "AU")).toEither
        .flatMap { phoneNumber =>
          if (phoneNumberUtil.isValidNumber(phoneNumber)) Right(new PhoneNumber(input))
          else Left(new IllegalArgumentException(s"$input is NOT a valid phone number"))
        }
  }

}
