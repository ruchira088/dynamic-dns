package com.ruchij.job.exceptions

import com.ruchij.job.services.dns.AwsRoute53Service.removeTrailingPeriods
import software.amazon.awssdk.services.route53.model.HostedZone

case class HostedZoneNotFoundForHostnameException(hostname: String, hostedZones: Seq[HostedZone]) extends Exception {
  override def getMessage: String =
    s"Unable to find Hosted Zone for $hostname. Hosted Zones: [${hostedZones.map(zone => removeTrailingPeriods(zone.name())).mkString(", ")}]"
}
