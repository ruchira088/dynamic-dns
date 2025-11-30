package com.ruchij.job.services.dns

import java.net.InetAddress
import cats.effect.{Async, Sync}
import cats.implicits._
import cats.{Applicative, ApplicativeError}
import com.ruchij.job.exceptions.HostedZoneNotFoundForHostnameException
import com.ruchij.job.models.JobResult.DnsUpdated
import com.ruchij.job.services.dns.AwsRoute53Service.withName
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.route53.Route53AsyncClient
import software.amazon.awssdk.services.route53.model._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.language.postfixOps

class AwsRoute53Service[F[_]: Async](route53AsyncClient: Route53AsyncClient)(
  implicit executionContext: ExecutionContext
) extends DnsManagementService[F] {

  private val awsHostedZones: F[Seq[HostedZone]] =
    Async[F].async_[Seq[HostedZone]] { callback =>
      route53AsyncClient
        .listHostedZones()
        .asScala
        .onComplete {
          _.fold[Unit](
            throwable => callback(Left(throwable)),
            zones => callback(Right(zones.hostedZones().asScala.toSeq))
          )
        }
    }

  def upsert(hostedZoneId: String, hostname: String, ip: InetAddress): F[DnsUpdated] = {
    val resourceRecord = ResourceRecord.builder().value(ip.getHostAddress).build()
    val resourceRecordSet =
      ResourceRecordSet
        .builder()
        .`type`(RRType.A)
        .ttl(AwsRoute53Service.dnsTtl.toSeconds)
        .resourceRecords(resourceRecord)
        .name(hostname)
        .build()

    val change = Change.builder().action(ChangeAction.UPSERT).resourceRecordSet(resourceRecordSet).build()
    val changeBatch = ChangeBatch.builder().changes(change).build()
    val changeResourceRecordSetsRequest =
      ChangeResourceRecordSetsRequest
        .builder()
        .changeBatch(changeBatch)
        .hostedZoneId(hostedZoneId)
        .build()

    Async[F].async_[DnsUpdated] { callback =>
      route53AsyncClient
        .changeResourceRecordSets(changeResourceRecordSetsRequest)
        .asScala
        .onComplete { _.fold(throwable => callback(Left(throwable)), _ => callback(Right(DnsUpdated(hostname, ip)))) }
    }
  }

  override def upsert(hostname: String, ip: InetAddress): F[DnsUpdated] =
    for {
      hostedZones <- awsHostedZones

      hostedZone <- hostedZones
        .foldLeft[Option[HostedZone]](None) {
          case (matchedZone, hostedZone withName hostedZoneName) =>
            if (hostname.endsWith(hostedZoneName) && matchedZone.forall(_.name().length < hostedZone.name().length))
              Some(hostedZone)
            else matchedZone
        }
        .fold[F[HostedZone]](
          ApplicativeError[F, Throwable].raiseError(HostedZoneNotFoundForHostnameException(hostname, hostedZones))
        )(Applicative[F].pure)

      result <- upsert(hostedZone.id(), hostname, ip)

    } yield result

}

object AwsRoute53Service {
  private val dnsTtl: FiniteDuration = 300 seconds

  def create[F[_]: Async](implicit executionContext: ExecutionContext): F[AwsRoute53Service[F]] =
    Sync[F].delay(Route53AsyncClient.builder().region(Region.AWS_GLOBAL).build())
      .map { route53AsyncClient => new AwsRoute53Service[F](route53AsyncClient)}

  @tailrec
  def removeTrailingPeriods(input: String): String =
    if (input.endsWith(".")) removeTrailingPeriods(input.init) else input

  object withName {
    def unapply(hostedZone: HostedZone): Some[(HostedZone, String)] =
      Some((hostedZone, removeTrailingPeriods(hostedZone.name())))
  }
}
