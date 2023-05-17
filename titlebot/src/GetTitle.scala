package titlebot

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all.*
import org.http4s.*
import org.http4s.client.Client
import org.http4s.client.UnexpectedStatus
import org.http4s.dsl.io.*
import org.typelevel.ci.CIString
import scribe.cats.{io => log}

trait GetTitle[F[_]] {
  def get(uri: Uri)(using
      d: EntityDecoder[F, Title]
  ): F[Either[Throwable, Title]]
}

object GetTitle {
  def impl(c: Client[IO]): GetTitle[IO] = new GetTitle[IO] {
    def get(
        uri: Uri
    )(using d: EntityDecoder[IO, Title]): IO[Either[Throwable, Title]] =
      c.run(Request[IO](Method.GET, uri))
        .use(resp =>
          (resp.status match {
            case Status.Ok => EitherT(d.decode(resp, strict = false).value) // The compiler can't see through the DecodeResult type alias
            case Status.PermanentRedirect | Status.MovedPermanently |
                Status.Found =>
              EitherT
                .fromEither[IO](
                  resp.headers
                    .get(CIString("Location"))
                    .map(_.head.value)
                    .toRight(
                      ParseFailure("Missing Location header", s"Response returned redirect status ${resp.status} but didn't provide a `Location` in headers")
                    )
                    .flatMap(Uri.fromString)
                )
                .flatMap(uri => EitherT(get(uri)))
            case _ =>
              EitherT.leftT[IO, Title](
                UnexpectedStatus(resp.status, Method.GET, uri)
              )
          }).value
        )
        .redeem(_.asLeft, identity)
  }
}
