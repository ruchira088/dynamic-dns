package com.ruchij.job.models

import java.net.InetAddress

sealed trait JobResult {
  val hostname: String
  val ip: InetAddress

  val message: String
}

object JobResult {
  case class NoChange(hostname: String, ip: InetAddress) extends JobResult {
    override val message: String = s"No change in DNS ($hostname - ${ip.getHostAddress})"
  }

  case class DnsUpdated(hostname: String, ip: InetAddress) extends JobResult {
    override val message: String = s"DNS of $hostname was updated with ${ip.getHostAddress}"
  }
}
