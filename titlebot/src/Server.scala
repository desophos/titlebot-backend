package titlebot

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.http4s.server.middleware.Logger
import scribe.cats.{io => log}

object Server {
  def run = {
    for {
      client <- EmberClientBuilder.default[IO].build
      getTitle = GetTitle.impl(client)
      routes   = Routes.getTitleRoutes(getTitle).orNotFound

      logErrorsApp = ErrorHandling.Recover.total(
        ErrorAction.log(
          routes,
          log.debug(_, _),
          log.warn(_, _),
        )
      )

      finalHttpApp = Logger.httpApp(
        logHeaders = true,
        logBody = true,
        redactHeadersWhen = _ => false,
        logAction = Some(IO.println),
      )(logErrorsApp)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever
}
