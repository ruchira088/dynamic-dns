package com.ruchij.job.services.notification.models

trait NotificationChannel {
  type Message
  type DestinationID
}
