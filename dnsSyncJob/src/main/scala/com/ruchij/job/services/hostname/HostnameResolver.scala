package com.ruchij.job.services.hostname

import java.net.InetAddress

trait HostnameResolver[F[_]] {
  def ipAddress(hostname: String): F[Option[InetAddress]]
}
