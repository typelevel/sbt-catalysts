package org
package typelevel

import sbt._
import sbtcatalysts.CatalystsPlugin.autoImport._

object Dependencies {

  // Versions for libraries and packages
  // Package -> version
  val versions = Map[String, String](
    "algebra"        -> "0.3.1",
    "alleycats"      -> "0.1.2",
    "catalysts"      -> "0.1.0",
    "cats"           -> "0.2.0",
    "discipline"     -> "0.4",
    "export-hook"    -> "1.0.2",
    "kind-projector" -> "0.6.3",
    "machinist"      -> "0.4.1",
    "macro-compat"   -> "1.0.6",
    "monocle"        -> "1.2.0-M1",
    "paradise"       -> "2.1.0-M5",
    "refined"        -> "0.3.1",
    "scalacheck"     -> "1.12.5",
    "scalatest"      -> "3.0.0-M10",
    "scalac"         -> "2.11.7",
    "scalac_2.11"    -> "2.11.7",
    "scalac_2.10"    -> "2.10.6",
    "shapeless"      -> "2.2.5",
    "simulacrum"     -> "0.4.0",
    "specs2"         -> "3.6.4"
  )

  // library definitions and links to their versions
  // Note that one version may apply to more than one library.
  // Library name -> version key, org, library
  val libraries = Map[String, (String, String, String)](
    "algebra"               -> ("algebra"         , "org.spire-math"               , "algebra"),
    "algebra-laws"          -> ("algebra"         , "org.spire-math"               , "algebra-laws"),
    "algebra-std"           -> ("algebra"         , "org.spire-math"               , "algebra-std"),
    "alleycats"             -> ("alleycats"       , "org.typelevel"                , "alleycats"),
    "catalysts"             -> ("catalysts"       , "org.typelevel"                , "catalysts"),
    "catalysts-checklite"   -> ("catalysts"       , "org.typelevel"                , "catalysts-checklite"),
    "catalysts-lawkit"      -> ("catalysts"       , "org.typelevel"                , "catalysts-lawkit"),
    "catalysts-macros"      -> ("catalysts"       , "org.typelevel"                , "catalysts-macros"),
    "catalysts-platform"    -> ("catalysts"       , "org.typelevel"                , "catalysts-platform"),
    "catalysts-scalatest"   -> ("catalysts"       , "org.typelevel"                , "catalysts-scalatest"),
    "catalysts-specbase"    -> ("catalysts"       , "org.typelevel"                , "catalysts-specbase"),
    "catalysts-speclite"    -> ("catalysts"       , "org.typelevel"                , "catalysts-speclite"),
    "catalysts-specs2"      -> ("catalysts"       , "org.typelevel"                , "catalysts-specs2"),
    "catalysts-testkit"     -> ("catalysts"       , "org.typelevel"                , "catalysts-testkit"),
    "cats"                  -> ("cats"            , "org.spire-math"               , "cats"),
    "cats-core"             -> ("cats"            , "org.spire-math"               , "cats-core"),
    "cats-free"             -> ("cats"            , "org.spire-math"               , "cats-free"),
    "cats-laws"             -> ("cats"            , "org.spire-math"               , "cats-laws"),
    "cats-macros"           -> ("cats"            , "org.spire-math"               , "cats-macros"),
    "cats-state"            -> ("cats"            , "org.spire-math"               , "cats-state"),
    "discipline"            -> ("discipline"      , "org.typelevel"                , "discipline"),
    "export-hook"           -> ("export-hook"     , "org.typelevel"                , "export-hook"),
    "machinist"             -> ("machinist"       , "org.typelevel"                , "machinist"),
    "macro-compat"          -> ("macro-compat"    , "org.typelevel"                , "macro-compat"),
    "monocle-core"          -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-core"),
    "monocle-generic"       -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-generic"),
    "monocle-macro"         -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-macro"),
    "monocle-state"         -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-state"),
    "monocle-law"           -> ("monocle"         , "com.github.julien-truffaut"   , "monocle-law"),
    "refined"               -> ("refined"         , "eu.timepit"                   , "refined"),
    "refined-scalacheck"    -> ("refined"         , "eu.timepit"                   , "refined-scalacheck"),
    "scalatest"             -> ("scalatest"       , "org.scalatest"                , "scalatest"),
    "scalacheck"            -> ("scalacheck"      , "org.scalacheck"               , "scalacheck"),
    "shapeless"             -> ("shapeless"       , "com.chuusai"                  , "shapeless"),
    "simulacrum"            -> ("simulacrum"      , "com.github.mpilquist"         , "simulacrum"),
    "specs2-core"           -> ("specs2"          , "org.specs2"                   , "specs2-core"),
    "specs2-scalacheck"     -> ("specs2"          , "org.specs2"                   , "specs2-scalacheck")
  )

  // compiler plugins definitions and links to their versions
  // Note that one version may apply to more than one plugin.
  // Library name -> version key, org, librar, crossVersion
  val scalacPlugins = Map[String, (String, String, String, CrossVersion)](
    "kind-projector"    -> ("kind-projector"  , "org.spire-math"      , "kind-projector" , CrossVersion.binary),
    "paradise"          -> ("paradise"        , "org.scalamacros"     , "paradise"       , CrossVersion.full)
  )

  // Some helper methods to combine libraries
  def macroCompatSettings(v: Versions): Seq[Setting[_]] =
    addCompileLibs(v, "macro-compat") ++ addCompilerPlugins(v, "paradise") ++
      scalaMacroDependencies(v)
}
