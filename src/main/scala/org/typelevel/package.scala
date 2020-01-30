package org
package typelevel

import sbt._
import sbtcatalysts.Libraries


object `package` {
  val typeLevelOrg = "org.typelevel"
  val libraries = Libraries()
    .add(name = "algebra",          version = "2.0.0",  org = typeLevelOrg, "algebra", "algebra-laws")
    .add(name = "discipline-core",  version = "1.0.2", org = typeLevelOrg)
    .add(name = "catalysts",        version = "0.8",    org = typeLevelOrg, "catalysts-checklite", "catalysts-lawkit", "catalysts-macros", "catalysts-platform", "catalysts-scalatest", "catalysts-specbase", "catalysts-speclite", "catalysts-specs2", "catalysts-testkit")
    .add(name = "cats",             version = "2.1.0",org = typeLevelOrg, "cats-core", "cats-kernel", "cats-free", "cats-laws", "cats-macros", "cats-testkit", "alleycats-core")
    .add(name = "cats-effect",      version = "2.0.0",org = typeLevelOrg, "cats-effect", "cats-effect-laws")
    .add(name = "cats-mtl",         version = "0.7.0",  org = typeLevelOrg, "cats-mtl-core", "cats-mtl-laws")
    .add(name = "cats-tagless",     version = "0.11",    org = typeLevelOrg, "cats-tagless-core", "cats-tagless-laws", "cats-tagless-macros")
    .add(name = "cats-collections", version = "0.9.0",  org = typeLevelOrg, "cats-collections-core")
    .add(name = "fs2" ,             version = "2.2.2",  org = "co.fs2", modules = "fs2-core", "fs2-io", "fs2-reactive-streams")
    .add(name = "http4s" ,          version = "0.20.17", org = "org.http4s", modules = "http4s-dsl", "http4s-blaze-server", "http4s-blaze-client", "http4s-play-json", "http4s-circe")
    .add(name = "kittens",          version = "2.0.0",  org = typeLevelOrg)
    .add(name = "mouse",            version = "0.24",   org = typeLevelOrg)
    .add(name = "machinist",        version = "0.6.8",  org = typeLevelOrg)
    .add(name = "macro-compat",     version = "1.1.1",  org = typeLevelOrg)
    .add(name = "monocle",          version = "1.5.1-cats",  org = "com.github.julien-truffaut", "monocle-core", "monocle-generic", "monocle-macro", "monocle-state", "monocle-law")
    .add(name = "newtype",          version = "0.4.3",  org = "io.estatico")
    .add(name = "refined",          version = "0.9.12",  org = "eu.timepit", "refined", "refined-scalacheck", "refined-scodec")
    .add(name = "scalacheck",       version = "1.14.3", org = "org.scalacheck")
    .add(name = "scalatest",        version = "3.1.0",  org = "org.scalatest")
    .add(name = "scodec",           version = "1.11.4", org = "org.scodec", "scodec-core")
    .add(name = "shapeless",        version = "2.3.3",  org = "com.chuusai")
    .add(name = "simulacrum",       version = "1.0.0", org = "org.typelevel")
    .add(name = "specs2",           version = "4.8.3",  org = "org.specs2", "specs2-core", "specs2-scalacheck", "specs2-mock")
    .add(name = "scalac"   ,        version = "2.12.10")
    .add(name = "scalac_2.13",      version = "2.13.0")
    .add(name = "scalac_2.12",      version = "2.12.10")
    .add(name = "scalac_2.11",      version = "2.11.12")
    .add(name = "scalac_2.10",      version = "2.10.7")
    .addScalacPlugin(name = "kind-projector", version = "0.10.3",  org = typeLevelOrg, crossVersion = CrossVersion.binary )
    .addScalacPlugin(name = "paradise",       version = "2.1.0", org = "org.scalamacros", crossVersion = CrossVersion.full )

}

