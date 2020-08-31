package com.ruchij.job.exceptions

import java.net.InetAddress

case class MyIpHostAddressMismatchException(apiInetAddress: InetAddress, awsInetAddress: InetAddress) extends Exception {
  override def getMessage: String =
    s"API ip=${apiInetAddress.getHostAddress} does not equal AWS ip=${awsInetAddress.getHostAddress}"
}
