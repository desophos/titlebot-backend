package titlebot

import cats.effect.IO
import org.http4s.*
import org.http4s.client.Client
import org.http4s.dsl.io.*

trait GetTitle[F[_]] {
  def get(uri: Uri): F[Option[Title]]
}

object GetTitle {
  def impl(c: Client[IO]): GetTitle[IO] = new GetTitle[IO] {
    def get(uri: Uri) = c.expect[Title](uri).redeem(_ => None, Some.apply)
  }
}
