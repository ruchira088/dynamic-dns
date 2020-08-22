package com.ruchij.job.models

sealed trait JobResult

object JobResult {
  case object NoChange extends JobResult
  case object DnsUpdated extends JobResult
}
