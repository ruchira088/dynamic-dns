package com.ruchij.api.exceptions

case object RemoteIpNotDeterminableException extends Exception {
  override def getMessage: String = "Unable to determine IP"
}
