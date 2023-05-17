package titlebot

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.http4s.server.middleware.Logger
import scribe.cats.{io => log}

object Server {
  def run = {
    val addCors = CORS.policy.withAllowOriginAll.apply[IO, IO]

    def addErrorLogging(app: Http[IO, IO]) =
      ErrorHandling.Recover.total(
        ErrorAction.log(
          app,
          log.debug(_, _),
          log.warn(_, _),
        )
      )

    val addHttpLogging = Logger.httpApp(
      logHeaders = true,
      logBody = true,
      redactHeadersWhen = _ => false,
      logAction = Some(IO.println),
    )

    for {
      client <- EmberClientBuilder.default[IO].build
      getTitle = GetTitle.impl(client)
      routes   = Routes.getTitleRoutes(getTitle).orNotFound
      app = (addCors compose addErrorLogging compose addHttpLogging)(routes)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
    } yield ()
  }.useForever
}
