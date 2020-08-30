package com.ruchij.job.services.ip

import java.net.InetAddress

trait MyIpRetriever[F[_]] {
  val ip: F[InetAddress]
}