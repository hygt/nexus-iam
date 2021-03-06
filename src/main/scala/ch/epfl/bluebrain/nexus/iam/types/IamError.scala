package ch.epfl.bluebrain.nexus.iam.types

import ch.epfl.bluebrain.nexus.iam.auth.TokenRejection
import ch.epfl.bluebrain.nexus.iam.config.Contexts.errorCtxUri
import ch.epfl.bluebrain.nexus.rdf.Iri.AbsoluteIri
import ch.epfl.bluebrain.nexus.rdf.instances.absoluteIriEncoder
import ch.epfl.bluebrain.nexus.rdf.syntax._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveEncoder
import io.circe.{Encoder, Json}

/**
  * Generic error types global to the entire service.
  *
  * @param msg the reason why the error occurred
  */
@SuppressWarnings(Array("IncorrectlyNamedExceptions"))
sealed abstract class IamError(val msg: String) extends Exception with Product with Serializable {
  override def fillInStackTrace(): Throwable = this
  override def getMessage: String            = msg
}

@SuppressWarnings(Array("IncorrectlyNamedExceptions"))
object IamError {

  /**
    * Signals the failure to perform an action on a resource, because of lacking permission.
    *
    * @param resource   the resource on which the action was attempted
    * @param permission the missing permission
    */
  final case class AccessDenied(resource: AbsoluteIri, permission: Permission)
      extends IamError(s"Access '${permission.value}' to resource '${resource.asUri}' denied.")

  /**
    * Signals an unexpected state was detected after a command evaluation.
    *
    * @param resource the resource on which the action was attempted
    */
  final case class UnexpectedInitialState(resource: AbsoluteIri)
      extends IamError(s"Unexpected state on resource '${resource.asUri}'.")

  /**
    * Signals an internal timeout.
    *
    * @param reason a descriptive message on the operation that timed out
    */
  final case class OperationTimedOut(reason: String) extends IamError(reason)

  /**
    * Generic wrapper for iam errors that should not be exposed to clients.
    *
    * @param reason the underlying error reason
    */
  final case class InternalError(reason: String) extends IamError(reason)

  /**
    * Signals that an error occurred while attempting to perform an operation with an invalid access token.
    *
    * @param rejection a reason for why the token is considered invalid
    */
  final case class InvalidAccessToken(rejection: TokenRejection)
      extends IamError("The provided access token is invalid.")

  /**
    * Signals that the requested resource was not found
    */
  final case object NotFound extends IamError("The requested resource could not be found.")

  implicit val iamErrorEncoder: Encoder[IamError] = {
    implicit val rejectionConfig: Configuration = Configuration.default.withDiscriminator("@type")
    val enc                                     = deriveEncoder[IamError].mapJson(_ addContext errorCtxUri)
    Encoder.instance(r => enc(r) deepMerge Json.obj("reason" -> Json.fromString(r.msg)))
  }
}
