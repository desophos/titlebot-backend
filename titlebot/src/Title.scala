package titlebot

import cats.data.EitherT
import cats.effect.IO
import org.http4s.*

final case class Title(value: String) extends AnyVal

object Title {
  val pattern = raw"<title>(.*?)<\/title>".r
}

given EntityDecoder[IO, Title] =
  EntityDecoder
    .text[IO]
    .flatMapR(s =>
      EitherT.fromOption(
        Title.pattern
          .findFirstMatchIn(s)
          .map(_.group(1))
          .map(Title.apply),
        InvalidMessageBodyFailure("No title found"),
      )
    )

given EntityEncoder[IO, Title] = EntityEncoder.stringEncoder.contramap(_.value)
