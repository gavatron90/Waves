package com.wavesplatform.lang.contract

import com.wavesplatform.lang.contract.DApp.{CallableFunction, DefaultFunction, VerifierFunction}
import com.wavesplatform.lang.v1.compiler.CompilationError.Generic
import com.wavesplatform.lang.v1.compiler.Terms.DECLARATION
import com.wavesplatform.lang.v1.compiler.Types._
import com.wavesplatform.lang.v1.compiler.{CompilationError, Terms}
import com.wavesplatform.lang.v1.evaluator.ctx.impl.waves.WavesContext

case class DApp(
    decs: List[DECLARATION],
    callableFuncs: List[CallableFunction],
    defaultFuncOpt: Option[DefaultFunction],
    verifierFuncOpt: Option[VerifierFunction]
)

object DApp {

  sealed trait Annotation {
    def invocationArgName: String
    def dic: Map[String, FINAL]
  }
  object Annotation {
    def parse(name: String, args: List[String]): Either[CompilationError, Annotation] = {
      (name, args) match {
        case ("Verifier", s :: Nil) => Right(VerifierAnnotation(s))
        case ("Verifier", s :: xs)  => Left(Generic(0, 0, "Incorrect amount of bound args in Verifier, should be one, e.g. @Verifier(tx)"))
        case ("Default", s :: Nil)  => Right(DefaultFuncAnnotation(s))
        case ("Default", s :: xs)   => Left(Generic(0, 0, "Incorrect amount of bound args in Default, should be one, e.g. @Default(inv)"))
        case ("Callable", s :: Nil) => Right(CallableAnnotation(s))
        case ("Callable", s :: xs)  => Left(Generic(0, 0, "Incorrect amount of bound args in Callable, should be one, e.g. @Callable(inv)"))
        case _                      => Left(Generic(0, 0, "Annotation not recognized"))
      }
    }

    def validateAnnotationSet(l: List[Annotation]): Either[CompilationError, Unit] = {
      l match {
        case (v: VerifierAnnotation) :: Nil    => Right(())
        case (d: DefaultFuncAnnotation) :: Nil => Right(())
        case (c: CallableAnnotation) :: Nil    => Right(())
        case _                                 => Left(Generic(0, 0, "Unsupported annotation set"))
      }
    }
  }
  case class CallableAnnotation(invocationArgName: String) extends Annotation {
    lazy val dic = Map(invocationArgName -> com.wavesplatform.lang.v1.evaluator.ctx.impl.waves.Types.invocationType)
  }
  case class DefaultFuncAnnotation(invocationArgName: String) extends Annotation {
    lazy val dic = Map(invocationArgName -> com.wavesplatform.lang.v1.evaluator.ctx.impl.waves.Types.invocationType)
  }
  case class VerifierAnnotation(invocationArgName: String) extends Annotation {
    lazy val dic = Map(invocationArgName -> WavesContext.verifierInput)
  }

  sealed trait AnnotatedFunction {
    def annotation: Annotation
    def u: Terms.FUNC
  }
  case class CallableFunction(override val annotation: CallableAnnotation, override val u: Terms.FUNC)   extends AnnotatedFunction
  case class DefaultFunction(override val annotation: DefaultFuncAnnotation, override val u: Terms.FUNC) extends AnnotatedFunction
  case class VerifierFunction(override val annotation: VerifierAnnotation, override val u: Terms.FUNC)   extends AnnotatedFunction
}
