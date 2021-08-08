package com.ruchij.job.services.notification

import com.ruchij.job.services.notification.models.NotificationChannel

trait NotificationService[F[_], Channel <: NotificationChannel] {
  type NotificationResult

  def send(destinationID: Channel#DestinationID, message: Channel#Message): F[NotificationResult]
}
