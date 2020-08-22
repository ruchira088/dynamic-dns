package com.ruchij.job.services.ip

import java.net.InetAddress

trait HostIpRetriever[F[_]] {
  val hostIp: F[InetAddress]
}