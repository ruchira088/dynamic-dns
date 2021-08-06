package com.ruchij.core.exceptions

case object OutcomeCancelledException extends Exception {
  override def getMessage: String = "Outcome has been cancelled"
}
