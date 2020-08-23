package com.ruchij.job.services.dns

import java.net.InetAddress

import com.ruchij.job.models.JobResult.DnsUpdated

trait DnsManagementService[F[_]] {
  def upsert(hostname: String, ip: InetAddress): F[DnsUpdated]
}
