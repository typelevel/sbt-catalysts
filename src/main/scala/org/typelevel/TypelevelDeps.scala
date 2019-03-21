package org
package typelevel

import sbt._
import sbtcatalysts.CatalystsPlugin.autoImport._


object `package` {
  val typeLevelOrg = "org.typelevel"
  val libraries = Versions()
    .add(name = "algebra",          version = "1.0.0",  org = typeLevelOrg, "algebra", "algebra-laws")
    .add(name = "catalysts",        version = "0.7",    org = typeLevelOrg, "catalysts-checklite", "catalysts-lawkit", "catalysts-macros", "catalysts-platform", "catalysts-scalatest", "catalysts-specbase", "catalysts-speclite", "catalysts-specs2", "catalysts-testkit")
    .add(name = "cats",             version = "1.6.0",  org = typeLevelOrg, "cats-core", "cats-kernel", "cats-free", "cats-laws", "cats-macros", "cats-testkit", "alleycats")
    .add(name = "cats-effect",      version = "1.2.0",  org = typeLevelOrg)
    .add(name = "cats-mtl",         version = "0.5.0",  org = typeLevelOrg, "cats-mtl-core", "cats-mtl-laws")
    .add(name = "cats-collection",  version = "0.7.0",  org = typeLevelOrg)
    .add(name = "kittens",          version = "1.2.0",  org = typeLevelOrg)
    .add(name = "mouse",            version = "0.16",   org = typeLevelOrg)
    .add(name = "discipline",       version = "0.9.0",  org = typeLevelOrg)
    .add(name = "machinist",        version = "0.6.5",  org = typeLevelOrg)
    .add(name = "macro-compat", version = "1.1.1",  org = typeLevelOrg)
    .add(name = "monocle",          version = "1.5.1-cats", org = "com.github.julien-truffaut", "monocle-core", "monocle-generic", "monocle-macro", "monocle-state", "monocle-law")
    .add(name = "refined",          version = "0.8.7",  org = "eu.timepit", "refined", "refined-scalacheck", "refined-scodec")
    .add(name = "scalacheck",       version = "1.13.5", org = "org.scalacheck")
    .add(name = "scalatest",        version = "3.0.5",  org = "org.scalatest")
    .add(name = "scodec",           version = "1.10.3", org = "org.scodec", "scodec-core", "scodec-protocols", "scodec-stream", "scodec-spire", "scodec-bits")
    .add(name = "shapeless",        version = "2.3.3",  org = "com.chuusai")
    .add(name = "simulacrum",       version = "0.14.0", org = "com.github.mpilquist")
    .add(name = "specs2",           version = "3.9.2",  org = "org.specs2", "specs2-core", "specs2-scalacheck")
    .add(name = "scalac"   ,        version = "2.12.7")
    .add(name = "scalac_2.13",      version = "2.13.0-M5")
    .add(name = "scalac_2.12",      version = "2.12.7")
    .add(name = "scalac_2.11",      version = "2.11.12")
    .add(name = "scalac_2.10",      version = "2.10.7")
    .addScalacPlugin(name = "kind-projector", version = "0.9.9",  org = "org.spire-math", crossVersion = CrossVersion.binary )
    .addScalacPlugin(name = "paradise",       version = "2.1.0", org = "org.scalamacros", crossVersion = CrossVersion.full )

  def macroCompatSettings(v: Versions): Seq[Setting[_]] =
    addCompileLibs(v, "macro-compat") ++ paradiseSettings(v) ++ // addCompilerPlugins(v, "paradise") ++
      scalaMacroDependencies(v)
}


/** Default Typelevel dependencies.*/
@deprecated("use org.typelevel.libraries instead", "0.10.0")
object Dependencies {

  /**
   * Versions for libraries and packages
   *
   * Format: Package -> version
   */
  val versions = typelevel.libraries.vers

  /**
   * Library definitions and links to their versions.
   *
   * Note that one version may apply to more than one library.
   * Format: Library name -> version key, org, library
   */
  val libraries = typelevel.libraries.libs

  /**
   * Compiler plugins definitions and links to their versions
   *
   * Note that one version may apply to more than one plugin.
   *
   * Format: Library name -> version key, org, librar, crossVersion
   */
  val scalacPlugins = typelevel.libraries.plugs

  // Some helper methods to combine libraries
  /**
   * Sets all settings required for the macro-compat library.
   *
   * @param v Versions map to use
   * @return All settings required for the macro-compat library
   */
  def macroCompatSettings(v: Versions): Seq[Setting[_]] = org.typelevel.macroCompatSettings(v)
}
