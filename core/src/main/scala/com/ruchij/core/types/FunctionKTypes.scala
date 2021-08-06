package com.ruchij.core.types

import cats.effect.Outcome
import cats.{Applicative, ApplicativeError, ~>}
import com.ruchij.core.exceptions.OutcomeCancelledException

object FunctionKTypes {

  implicit def eitherToF[L, F[_]: ApplicativeError[*[_], L]]: Either[L, *] ~> F =
    new ~>[Either[L, *], F] {
      override def apply[A](either: Either[L, A]): F[A] =
        either.fold(ApplicativeError[F, L].raiseError, Applicative[F].pure)
    }

  implicit class OutcomeOps[F[_]: ApplicativeError[*[_], Throwable], A](val outcome: Outcome[F, Throwable, A]) {
    val toF: F[A] =
      outcome.fold(
        ApplicativeError[F, Throwable].raiseError(OutcomeCancelledException),
        ApplicativeError[F, Throwable].raiseError,
        identity
      )
  }

  def identityFunctionK[F[_]]: F ~> F = new ~>[F, F] {
    override def apply[A](fa: F[A]): F[A] = fa
  }
}
