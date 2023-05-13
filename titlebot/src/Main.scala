package titlebot

import cats.effect.IOApp
import scribe.Level
import scribe.cats.{io => log}

object Main extends IOApp.Simple {
  scribe.Logger.root
    .clearHandlers()
    .clearModifiers()
    .withHandler(minimumLevel = Some(Level.Debug))
    .replace()

  val run = Server.run
}
