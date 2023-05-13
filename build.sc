import mill._
import mill.scalalib._
import scalafmt._

object titlebot extends ScalaModule {
  def scalaVersion = "3.2.2"

  def catsCoreV   = "2.8.0"
  def catsEffectV = "3.3.14"
  def http4sV     = "0.23.18"
  def circeV      = "0.14.5"
  def scribeV     = "3.10.4"

  def scalacOptions = Seq(
    "-explain",
    "-explain-types",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-no-indent",
    "-Xfatal-warnings",
    "-Ysafe-init",
  )

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:$catsCoreV",
    ivy"org.typelevel::cats-effect:$catsEffectV",
    ivy"org.http4s::http4s-ember-client:$http4sV",
    ivy"org.http4s::http4s-ember-server:$http4sV",
    ivy"org.http4s::http4s-dsl:$http4sV",
    ivy"org.http4s::http4s-circe:$http4sV",
    ivy"io.circe::circe-core:$circeV",
    ivy"com.outr::scribe:$scribeV",
    ivy"com.outr::scribe-cats:$scribeV",
    ivy"com.outr::scribe-slf4j:$scribeV",
  )

  object test extends Tests with TestModule.Munit {
    def munitV           = "1.0.0-M7"
    def munitCatsEffectV = "2.0.0-M3"

    def ivyDeps = Agg(
      ivy"org.scalameta::munit:$munitV",
      ivy"org.scalameta::munit-scalacheck:$munitV",
      ivy"org.typelevel::munit-cats-effect:$munitCatsEffectV",
    )
  }
}
