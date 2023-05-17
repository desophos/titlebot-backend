package titlebot

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import scribe.cats.{io => log}

given EntityDecoder[IO, Uri] = EntityDecoder
  .text[IO]
  .flatMapR(s =>
    EitherT.fromEither(
      Uri
        .fromString(s)
        .leftMap(failure =>
          InvalidMessageBodyFailure(failure.details, failure.cause)
        )
    )
  )

object Routes {
  def getTitleRoutes(getTitle: GetTitle[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] { case req @ POST -> Root =>
      req
        .as[Uri]
        .flatMap(getTitle.get)
        .flatMap {
          case Left(e)      => log.error(e) *> BadRequest("Invalid URL")
          case Right(title) => Ok(title)
        }
    }
}
