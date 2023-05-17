package titlebot

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import cats.data.EitherT
import cats.effect.IO
import org.http4s.*
import org.jsoup.Jsoup

final case class Title(value: String) extends AnyVal

given EntityDecoder[IO, Title] =
  EntityDecoder
    .text[IO]
    .flatMapR(s =>
      EitherT.fromEither(Try(Jsoup.parse(s).title()) match {
        case Failure(e) =>
          Left(InvalidMessageBodyFailure("No title found", Some(e)))
        case Success(s) => Right(Title(s))
      })
    )

given EntityEncoder[IO, Title] = EntityEncoder.stringEncoder.contramap(_.value)
