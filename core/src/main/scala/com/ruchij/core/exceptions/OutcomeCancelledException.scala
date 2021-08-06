package com.ruchij.api.exceptions

case object OutcomeCancelledException extends Exception {
  override def getMessage: String = "Outcome has been cancelled"
}
