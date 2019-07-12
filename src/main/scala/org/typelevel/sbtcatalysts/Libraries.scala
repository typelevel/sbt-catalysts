package org.typelevel.sbtcatalysts

import sbt.Keys.libraryDependencies
import sbt.{CrossVersion, Def, ExclusionRule, ModuleID, Setting}
import Libraries._
import sbt._
import sbtcrossproject.CrossPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._


/** Container for the version, library and scala plugin Maps.*/
case class Libraries(vers: VersionsType = Map(),
                     libs: LibrariesType = Map(),
                     plugs: ScalacPluginType = Map(),
                     libsSprt: Map[String, LibrarySupport] = Map()) {

  def add(name: String, librarySupport: LibrarySupport): Libraries =
    copy(libsSprt = libsSprt + (name -> librarySupport))

  def add(name: String, version: String, org: String): Libraries =
    add(name, version, LibrarySupport.ScalaJS, org)

  def addJVM(name: String, version: String, org: String): Libraries =
    add(name, version, LibrarySupport.ScalaJVM, org)

  def addJava(name: String, version: String, org: String): Libraries =
    add(name, version, LibrarySupport.Java, org)

  def add(name: String, version: String): Libraries =
    copy(vers = vers + (name -> version))

  def add(name: String, version: String, librarySupport: LibrarySupport, org: String): Libraries =
    add(name, version)
      .copy(libs = libs + (name -> (name, org, name)))
      .add(name, librarySupport)

  def add(name: String, version: String, org: String, modules: String*): Libraries =
    add(name, version, org, LibrarySupport.ScalaJS, modules:_*)

  def addJVM(name: String, version: String, org: String, modules: String*): Libraries =
    add(name, version, org, LibrarySupport.ScalaJVM, modules:_*)

  def addJava(name: String, version: String, org: String, modules: String*): Libraries =
    add(name, version, org, LibrarySupport.ScalaJVM, modules:_*)

  def add(name: String, version: String, org: String, librarySupport: LibrarySupport, modules: String*): Libraries =
    add(name, version)
      .add(name, librarySupport)
      .copy(libs = libs ++ modules.map(module => module -> (name, org, module)).toMap)

  def addScalacPlugin(name: String, version: String, org: String, crossVersion: CrossVersion) =
    copy(plugs = plugs + (name -> (name, org, name, crossVersion)), vers = vers + (name -> version))

  def +(other: Libraries) = copy(vers ++ other.vers, libs ++ other.libs, plugs ++ other.plugs)

  def moduleID(key: String) = Def.setting {

    val (libKey, libOrg, libModuleName) = libs(key)

    val (libVer, libSpt) = (vers(libKey), libsSprt(libKey))

    libSpt match {
      case LibrarySupport.Java =>
        libOrg % libModuleName % libVer
      case LibrarySupport.ScalaJVM =>
        libOrg %% libModuleName % libVer
      case LibrarySupport.ScalaJS =>
        libOrg %%% libModuleName % libVer
    }
  }

  def dependency(moduleName: String,
                 maybeScope: Option[String] = None,
                 exclusions: List[ExclusionRule] = Nil): Setting[Seq[ModuleID]] =
    libraryDependencies += {
      val m = moduleID(moduleName).value
      (maybeScope, exclusions) match {
        case (Some(scope), Nil) => m % scope
        case (None, ex) => m excludeAll (ex: _*)
        case (Some(scope), ex) => m % scope excludeAll (ex: _*)
        case _ => m
      }
    }

  def testDependencies(moduleNames: String*): Seq[Setting[Seq[ModuleID]]] =
    moduleNames.map(dependency(_, Some("test")))

  def dependencies(moduleNames: String*): Seq[Setting[Seq[ModuleID]]] =
    moduleNames.map(dependency(_))
}

object Libraries {
  type VersionsType = Map[String, String]
  type LibrariesType = Map[String, (String, String, String)]
  type ScalacPluginType = Map[String, (String, String, String, CrossVersion)]

}

sealed trait LibrarySupport extends Serializable with Product
object LibrarySupport {
  case object ScalaJS extends LibrarySupport
  case object ScalaJVM extends LibrarySupport
  case object Java extends LibrarySupport
}
