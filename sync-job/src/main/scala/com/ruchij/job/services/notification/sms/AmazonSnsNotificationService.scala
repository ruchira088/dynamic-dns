package com.ruchij.job.services.notification.sms

import cats.effect.{Async, Sync}
import cats.implicits._
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{MessageAttributeValue, PublishRequest, PublishResponse}

import scala.jdk.CollectionConverters.MapHasAsJava

class AmazonSnsNotificationService[F[_]: Async](snsAsyncClient: SnsAsyncClient) extends SmsNotificationService[F] {

  override type NotificationResult = PublishResponse

  private val MessageAttributes =
    Map(
      "AWS.SNS.SMS.SenderID" -> MessageAttributeValue.builder().stringValue("Dynamic-DNS").dataType("String").build(),
      "AWS.SNS.SMS.SMSType" -> MessageAttributeValue.builder().stringValue("Transactional").dataType("String").build()
    )

  override def send(destinationID: SmsNotificationService.PhoneNumber, message: String): F[PublishResponse] =
    Async[F].fromCompletableFuture {
      Sync[F].delay {
        snsAsyncClient.publish {
          PublishRequest.builder()
            .message(message)
            .phoneNumber(destinationID.value)
            .messageAttributes(MessageAttributes.asJava)
            .build()
        }
      }
    }

}

object AmazonSnsNotificationService {
  def create[F[_]: Async]: F[AmazonSnsNotificationService[F]] =
    Sync[F].delay(SnsAsyncClient.create())
      .map { snsAsyncClient => new AmazonSnsNotificationService[F](snsAsyncClient) }
}
