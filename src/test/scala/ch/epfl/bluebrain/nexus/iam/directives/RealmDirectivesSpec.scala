package ch.epfl.bluebrain.nexus.iam.directives

import java.net.URLEncoder

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch.epfl.bluebrain.nexus.commons.http.JsonLdCirceSupport._
import ch.epfl.bluebrain.nexus.iam.directives.RealmDirectives._
import ch.epfl.bluebrain.nexus.iam.routes.SearchParams
import ch.epfl.bluebrain.nexus.rdf.Iri.AbsoluteIri
import ch.epfl.bluebrain.nexus.rdf.instances._
import ch.epfl.bluebrain.nexus.rdf.syntax._
import io.circe.generic.auto._
import org.mockito.IdiomaticMockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{EitherValues, Matchers, WordSpecLike}

class RealmDirectivesSpec
    extends WordSpecLike
    with ScalatestRouteTest
    with Matchers
    with ScalaFutures
    with EitherValues
    with IdiomaticMockito {

  private def route: Route = {
    (get & searchParams) { params =>
      complete(params)

    }
  }

  private def encode(s: AbsoluteIri) = URLEncoder.encode(s.asString, "UTF-8")

  "Realm directives" should {
    "return query params" in {
      val createdBy: AbsoluteIri = url"http://example.com/created".value
      val updatedBy: AbsoluteIri = url"http://example.com/updated".value
      val tpe: AbsoluteIri       = url"http://example.com/tpe".value
      val tpe2: AbsoluteIri      = url"http://example.com/tpe2".value
      Get(s"/?rev=2&deprecated=true&type=${encode(tpe)}&type=${encode(tpe2)}&createdBy=${encode(createdBy)}&updatedBy=${encode(
        updatedBy)}") ~> route ~> check {
        responseAs[SearchParams] shouldEqual SearchParams(Some(true),
                                                          Some(2L),
                                                          Some(createdBy),
                                                          Some(updatedBy),
                                                          Set(tpe, tpe2))
      }
    }
  }

}
