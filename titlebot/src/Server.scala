package titlebot

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{Logger => ServerLogger}
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.http4s.server.middleware.Logger
import scribe.cats.{io => log}

object Server {
  def run = {
    val serverCors = CORS.policy.withAllowOriginAll.apply[IO, IO]

    def serverErrorLogging(app: Http[IO, IO]) =
      ErrorHandling.Recover.total(
        ErrorAction.log(
          app,
          log.debug(_, _),
          log.warn(_, _),
        )
      )

    val serverHttpLogging = ServerLogger.httpApp(
      logHeaders = true,
      logBody = true,
      redactHeadersWhen = _ => false,
      logAction = Some(log.debug(_)),
    )

    val serverMiddleware =
      (serverCors compose serverErrorLogging compose serverHttpLogging)

    for {
      client <- EmberClientBuilder.default[IO].build
      getTitle = GetTitle.impl(client)
      routes   = Routes.getTitleRoutes(getTitle).orNotFound
      app      = serverMiddleware(routes)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
    } yield ()
  }.useForever
}
