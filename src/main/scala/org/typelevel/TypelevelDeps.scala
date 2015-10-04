package org
package typelevel

object Dependencies {

  // Versions for libraries and packages
  // Package -> version
  val versions = Map[String, String](
    "discipline"   -> "0.4",
    "macro-compat" -> "1.0.2",
    "paradise"     -> "2.1.0-M5",
    "scalacheck"   -> "1.12.4",
    "scalatest"    -> "3.0.0-M7",
    "scalac"       -> "2.11.7",
    "scalac_2.11"  -> "2.11.7",
    "scalac_2.10"  -> "2.10.6",
    "specs2"       -> "3.6.4"
  )

  // library definitions and links to their versions
  // Note that one version may apply to more than one library.
  // Library name -> version key, org, library 
  val libraries = Map[String, (String, String, String)](
    "discipline"        -> ("discipline"  , "org.typelevel" , "discipline"),
    "macro-compat"      -> ("macro-compat", "org.typelevel" , "macro-compat"),
    "scalatest"         -> ("scalatest"   , "org.scalatest" , "scalatest"),
    "scalacheck"        -> ("scalacheck"  , "org.scalacheck", "scalacheck"),
    "specs2-core"       -> ("specs2"      , "org.specs2"    , "specs2-core"),
    "specs2-scalacheck" -> ("specs2"      , "org.specs2"    , "specs2-scalacheck")
  )
}
