package sbtcatalysts

import sbt.{Def, _}
import Keys.{name, _}
import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.site.SitePlugin.autoImport._
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.sbtghpages.GhpagesPlugin.autoImport._
import tut.TutPlugin
import tut.TutPlugin.autoImport._
import pl.project13.scala.sbt.SbtJmh._
import sbtunidoc.ScalaUnidocPlugin
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import sbtunidoc.BaseUnidocPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._
import org.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin.autoImport.{CrossType => DeprecatedCT, _}
import microsites.MicrositesPlugin.autoImport._
import scoverage.ScoverageKeys
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.{CrossProject, JVMPlatform}
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.JSPlatform
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import org.typelevel.sbtcatalysts.Libraries._
import org.typelevel.sbtcatalysts.Libraries

import sbtcatalysts.CatalystsPlugin.autoImport.{addCompileLibs, paradiseSettings, scalaMacroDependencies}
/**
 * Plugin that automatically brings into scope all predefined val's and method
 * definitions.
 * 
 * It is very important to realise that using the plugin does not actually change the
 * the build in any way, but merely provides the methods to help create a build.sbt.
 * 
 * Compared to a plugin that "does everything", the advantage of this approach is that it is far
 * easier to use other functionality where appropriate and also to "see" what the build is
 * is actually doing, as the methods are (mainly) just pure SBT methods.
 * 
 * Where we do deviate from "pure" SBT is how we define library dependencies. The norm would be:
 * 
 * In a small project, define the library dependencies explicitly:
 * 
 *    libraryDependencies += "org.typelevel" %% "alleycats" %  "0.1.0"
 * 
 * If another sub-project also has this dependency, it's common to move the definition to a val, and
 * often the version too. If the library is used in another module for test only, another val is 
 * required:
 * 
 *      val alleycatsV = "0.1.0"
 *      val alleycatsDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats" % alleycatsV)
 *      val alleycatsTestDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats" % alleycatsV % "test")
 * 
 * Whilst this works fine for individual projects, it's not ideal when a group of loosely coupled want
 * to share a common set (or sets) of dependencies, that they can also modify locally if required.
 * In this sense, we need "cascading configuration dependency files" and this is what this plugin also
 * provides.
 * 
 * "org.typelevel.dependencies" provides two Maps, one for library versions the the for individual libraries
 * with their organisation, name and version. Being standard scala Maps, other dependency Maps can be added
 * with new or updated dependencies. The same applies for scala plugins. To use, we create the three Maps and
 * add to a combined container and then add the required dependencies to a module: eg
 * 
 *     val vers = typelevel.versions ++ catalysts.versions + ("discipline" -> "0.3")
 *     val libs = typelevel.libraries ++ catalysts.libraries
 *     val addins = typelevel.scalacPlugins ++ catalysts.scalacPlugins
 *     val vAll = Versions(vers, libs, addins)
 *     ....
 *    .settings(addLibs(vAll, "specs2-core","specs2-scalacheck" ):_*)
 *    .settings(addTestLibs(vAll, "scalatest" ):_*)
 *    ...
 *    .settings(
  *       addLibsExcluding(
 *          vAll,
 *          exclusions = List(
 *            ExclusionRule("org.scalaz", "scalaz-concurrent"),
 *            ExclusionRule("org.specs2", "specs2-matcher")
 *        ),
 *        "specs2-core","specs2-scalacheck" ):_*)
 *
 */
object CatalystsPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

   object autoImport extends CatalystsBase {
  }
}


/** Contains all methods to simplify a common build file.*/
trait CatalystsBase {

  import CatalystsKeys._


  def singleModuleLib(name: String, org: String): (String, (String, String, String)) =
    name -> (name, org, name)

  def multiModuleLib(name: String, org: String, modules: String*): LibrariesType =
    modules.map(module => module -> (name, org, module)).toMap



  // Licences
  /** Apache 2.0 Licence.*/
  val apache = ("Apache License", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

  /** MIT Licence.*/
  val mit = ("MIT", url("http://opensource.org/licenses/MIT"))

  /** Github settings and related settings usually found in a Github README.*/
  case class GitHubSettings(org: String, proj: String, publishOrg: String, license: (String, URL)) {
    def home = s"https://github.com/$org/$proj"
    def repo = s"git@github.com:$org/$proj.git"
    def api  = s"https://$org.github.io/$proj/api/"
    def organisation  = s"com.github.$org"
    override def toString =
      s"GitHubSettings:home = $home\nGitHubSettings:repo = $repo\nGitHubSettings:api = $api\nGitHubSettings:organisation = $organisation"
  }

  /** Using the supplied Versions map, adds the list of libraries to a module.*/
  def addLibs(versions: Libraries, libs: String*): Seq[Def.Setting[Seq[ModuleID]]] =
    versions.dependencies(libs:_*)

  /** Using the supplied Versions map, adds the list of libraries to a module as a compile dependency.*/
  def addCompileLibs(versions: Libraries, libs: String*) =
    addLibsScoped(versions, "compile", libs:_*)

  /** Using the supplied Versions map, adds the list of libraries to a module as a test dependency.*/
  def addTestLibs(versions: Libraries, moduleNames: String*): Seq[Def.Setting[Seq[ModuleID]]] =
    versions.testDependencies(moduleNames:_*)

  /** Using versions map, adds the list of libraries to a module using the given dependency.*/
  def addLibsScoped(versions: Libraries, scope: String, moduleNames: String*) =
    moduleNames map (versions.dependency(_, Some(scope)))

  /** Using versions map, adds the list of libraries to a module using the given dependency.*/
  def addLibsExcluding(versions: Libraries, exclusions: List[ExclusionRule], libs: String*) =
    libs map (versions.dependency(_, exclusions = exclusions))

  def addLibsExcluding(versions: Libraries, scope: String, exclusions: List[ExclusionRule], libs: String*) =
    libs map (versions.dependency(_, Some(scope), exclusions))

  /** Using the supplied Versions map, adds the list of compiler plugins to a module.*/
  def addCompilerPlugins(v: Libraries, plugins: String*) =
    plugins.flatMap(s => Seq(libraryDependencies += compilerPlugin(
      v.plugs(s)._2 %% v.plugs(s)._3 % v.vers(v.plugs(s)._1) cross v.plugs(s)._4)))


  // Common and shared setting
  /** Settings to make the module not published*/
  lazy val noPublishSettings = Seq(
    skip in publish := true
  )

  /**
   * Default settings for the root module.
   *
   * Sets the root module name to root and sets the module not to be published
   */
  lazy val rootSettings = noPublishSettings ++ Seq(
    moduleName := "root"
  )

  lazy val scalaPartialVersion = Def setting (CrossVersion.partialVersion(scalaVersion.value).getOrElse(throw new Exception("Unexpected ScalaVersion value" + scalaVersion.value)))

  lazy val scalaVersionMajor =  Def setting (scalaPartialVersion.value._1)
  lazy val scalaVersionMinor = Def setting (scalaPartialVersion.value._2)


  /** Adds the settings required to add scala versioned shared directory.
   *
   * In a scala.js shared project, the shared sources are by default in a
   * the directory "shared/src/main/scala". By adding these settings, the build will also
   * look in the directories that match the scala version:
   *
   *      "shared/src/main/scala-2.10"
   *      "shared/src/main/scala-2.11"
   *      "shared/src/main/scala-2.12"
   */
  lazy val crossVersionSharedSources: Seq[Setting[_]] =
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc).value.map { dir =>
          new File(s"${dir.getPath}-${scalaVersionMajor.value}.${scalaVersionMinor}")

        }
      }
    }

  /** Using the supplied Versions map, adds the dependencies for scala macros.*/
  def scalaMacroDependencies(v: Libraries): Seq[Setting[_]] = {
    val version = v.vers("paradise")
    Seq(
       libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
       libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
       libraryDependencies ++= {
         CrossVersion.partialVersion(scalaVersion.value) match {
           // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
           case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
           // in Scala 2.10, quasiquotes are provided by macro paradise
           case Some((2, 10)) =>
             Seq(
               compilerPlugin("org.scalamacros" %% "paradise" % version cross CrossVersion.full),
               "org.scalamacros" %% "quasiquotes" % version cross CrossVersion.binary
             )
         }
       }
    ) ++ paradiseSettings(v)
  }

  def paradiseSettings(v: Libraries): Seq[Setting[_]] = {
    val version = v.vers("paradise")
    Seq(
       libraryDependencies ++= {
         CrossVersion.partialVersion(scalaVersion.value) match {
           // if scala 2.13+ is used, quasiquotes are merged into scala-reflect
           case Some((2, scalaMajor)) if scalaMajor >= 13 => Seq()
           // otherwise quasiquotes are provided by macro paradise
           case _ =>
             Seq(
               compilerPlugin("org.scalamacros" %% "paradise" % version cross CrossVersion.full)
             )
         }
       }
    ) ++ macroAnnotationsSettings
  }

  lazy val macroAnnotationsSettings = Seq(
    scalacOptions += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 =>
          "-Ymacro-annotations"
        case _ =>
          ""
      }
    }
  )


  def macroCompatSettings(v: Libraries): Seq[Setting[_]] =
    addCompileLibs(v, "macro-compat") ++ paradiseSettings(v) ++
      scalaMacroDependencies(v)

  def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _ => false
  }

  /** Common scalac options useful to most (if not all) projects.*/
  lazy val scalacCommonOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked"
  )

  /** Scalac options for additional language options.*/
  lazy val scalacLanguageOptions = Seq(
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros"
  )

  /** Scalac strict compilation options.*/
  lazy val scalacStrictOptions = Seq(
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

 lazy val scalacStrictOptions2_12 = Seq(
   "-Yno-adapted-args",
   "-Xfuture"
 )

  /** Combines all scalac options.*/
  lazy val scalacAllOptions: Seq[String] = scalacCommonOptions ++ scalacLanguageOptions ++ scalacStrictOptions

  def scalacAllOptionsFor(scalaVersion: String): Seq[String] = scalacAllOptions ++ {
    if(priorTo2_13(scalaVersion)) scalacStrictOptions2_12 else Nil
  }

  /** all scalac options as a settings including the partialUnification and xlint **/
  lazy val scalacAllSettings: Seq[Setting[_]] = Seq(
    scalacOptions ++= scalacAllOptionsFor(scalaVersion.value)
  ) ++ partialUnificationSettings ++ xlintSettings ++ warnUnusedImport


  /**
   * Settings common to all projects.
   *
   * Adds Sonatype release repository and "withCachedResolution" to the update options
   */
  lazy val sharedCommonSettings = Seq(
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
    ),
    updateOptions := updateOptions.value.withCachedResolution(true),
    travisBuild := scala.sys.env.get("TRAVIS").isDefined
  )

 /**
   * Scala JS settings shared by many projects.
   *
   * Forces the use of node.js in tests and batchmode under travis
   */
  lazy val sharedJsSettings = Seq(
    scalaJSStage in Global := FastOptStage,
    parallelExecution := false,
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    // batch mode decreases the amount of memory needed to compile scala.js code
    scalaJSOptimizerOptions := scalaJSOptimizerOptions.value.withBatchMode(travisBuild.value)
  )

  /**
   * Scala JVM settings shared by many projects.
   *
   * Currently empty.
   */
  lazy val sharedJvmSettings = Seq()

  /**
   * Build settings common to all projects.
   *
   * Uses the github settings and versions map to set the organisation,
   * scala version and cross versions
   */
  def sharedBuildSettings(gh: GitHubSettings, v: Libraries) = Seq(
    organization := gh.publishOrg,
    scalaVersion := v.vers("scalac"),
    crossScalaVersions := Seq(v.vers("scalac_2.11"), scalaVersion.value)
  )

  /**
   * Publish settings common to all projects.
   *
   * Uses the github settings and list of developers to set all publish settings
   * required to publish signed artifacts to Sonatype OSS repository
   */
  def sharedPublishSettings(gh: GitHubSettings): Seq[Setting[_]] = Seq(
    homepage := Some(url(gh.home)),
    licenses += gh.license,
    scmInfo :=  Some(ScmInfo(url(gh.home), "scm:git:" + gh.repo)),
    apiURL := Some(url(gh.api)),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("Snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("Releases" at nexus + "service/local/staging/deploy/maven2")
    },
    autoAPIMappings := true
  )

  /**
   * Release settings common to all projects.
   *
   * Adds a Sonatype release step to the default release steps
   */
  lazy val sharedReleaseProcess = Seq(
    releaseProcess := sharedReleaseSteps
  )

  lazy val sharedReleaseSteps = Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    releaseStepCommandAndRemaining("+clean"),
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges)

  def insertReleaseStep(step: ReleaseStep, before: ReleaseStep, steps: Seq[ReleaseStep] = sharedReleaseSteps): Seq[ReleaseStep] = {
    val i = steps.toList.indexOf(before)
    if(i < 0) throw new RuntimeException("Cannot find before step in steps")
    else {
      val (before, after) = steps.splitAt(i)
      (before :+ step) ++ after
    }
  }

  /** Common coverage settings, with minimum coverage defaulting to 80.*/
  def sharedScoverageSettings(min: Int = 80) = Seq(
    ScoverageKeys.coverageMinimum := min,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := scalaBinaryVersion.value != "2.10",
    ScoverageKeys.coverageEnabled := {
      if(priorTo2_13(scalaVersion.value))
        ScoverageKeys.coverageEnabled.value
      else
        false
    }
    // ScoverageKeys.coverageExcludedPackages := "catalysts\\.bench\\..*"
  )

  /** Common unidoc settings, adding the "-Ymacro-no-expand" scalac option.*/
  lazy val unidocCommonSettings = Seq(
    scalacOptions in (ScalaUnidoc, unidoc) +=  {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 12 =>
          "-Ymacro-expand:none"
        case _ =>
          "-Ymacro-no-expand"
      }
    },
  )

  /** Add the "unused import" warning to scala 2.11+, but not for the console.*/
  lazy val warnUnusedImport = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          Seq()
        case Some((2, 11)) =>
          Seq("-Ywarn-unused-import")
        case Some((2, n)) if n >= 12 =>
          Seq("-Ywarn-unused:imports")

      }
    },
    scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
    scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
  )

  /** Adds the credential settings required for sonatype releases.*/
  lazy val credentialSettings = Seq(
    // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )

  lazy val xlintSettings = Seq(
    scalacOptions += {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => "-Xlint:-unused,_"  //Xlint:unused warn against unused implicit evidence
        case _ => "-Xlint"
      }
    }
  )

  lazy val partialUnificationSettings = Seq(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11 | 12)) => Seq("-Ypartial-unification")
        case _ => Nil  //Xling:unused warn against unused implicit evidence
      }
    }
  )

  lazy val CompileTime = config("compile-time").hide

  /**
   * add simulacrum settings,
   * @param compileTimeOnly make the dependency compile time only but may affect IntelliJ IDEA's simularcrum support (might be fixed post v2017.3)
   */
  def simulacrumSettings(v: Libraries, compileTimeOnly: Boolean = true) = paradiseSettings(v) ++ macroAnnotationsSettings ++ {
    if(compileTimeOnly) Seq(
      libraryDependencies ++= Seq(
        "com.github.mpilquist" %%% "simulacrum" % v.vers("simulacrum") % CompileTime),
      ivyConfigurations += CompileTime,
      unmanagedClasspath in Compile ++= update.value.select(configurationFilter(CompileTime.name))
    ) else Seq(libraryDependencies ++= Seq(
      "com.github.mpilquist" %%% "simulacrum" % v.vers("simulacrum") % Provided )
    )
  }



  def micrositeSettings(gh: GitHubSettings, dev: Developer, siteDescription: String) = Seq(
    micrositeName := gh.proj,
    micrositeDescription := siteDescription,
    micrositeBaseUrl := gh.proj,
    micrositeGithubOwner := gh.org,
    micrositeGithubRepo := gh.proj,
    micrositeHighlightTheme := "atom-one-light",
    micrositeAuthor := dev.name
  )
  // Builder methods

  /**
   * Creates a module definition based on a Scala.js CrossProject
   *
   * The standard Scala.js CrossProject contains the real JVM and JS project
   * and can be used as a dependency of another CrossProject. But it not an
   * sbt Project, and cannot be used as one.
   *
   * So we introduce the concept of a Module, that is essentially a CrossProject
   * with the difference that the variable name is the directory name with the
   * suffix "M". This allows us to create a real project based on the directory name
   * that is an aggregate project for the underlying JS and JVM projects.
   *
   * Usage;
   *
   * In build.sbt, create two helper methods:
   *
   *   lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
   *   lazy val prj = mkPrjFactory(rootSettings)
   *
   * For each sub-project (here core is used as an example)
   *
   *   lazy val core    = prj(coreM)
   *   lazy val coreJVM = coreM.jvm
   *   lazy val coreJS  = coreM.js
   *   lazy val coreM   = module("core",CrossType.Pure)
   *     .dependsOn(testkitM)
   *     .settings(addTestLibs(vlibs, "scalatest"):_*)
   */
  def mkModule(proj: String, projConfig: CrossProject ⇒ CrossProject, id: String,
      crossType: CrossType = CrossType.Pure ): CrossProject = {

    val cpId = id.stripSuffix("M")

    CrossProject(cpId, new File(cpId))(JSPlatform, JVMPlatform).crossType(crossType)
     .settings(moduleName := s"$proj-$id")
    .configureCross(projConfig)
  }

  /**
   * Helper method for mkModule so that the main project name and configuration need not ne repeated for each module
   */
  def mkModuleFactory(proj: String, projConfig: CrossProject ⇒ CrossProject) =
    (id: String, crossType: CrossType) => mkModule(proj, projConfig, id, crossType)

  /**
   * Makes a modules aggregate project
   */
  def mkPrj(projSettings: Seq[sbt.Setting[_]], c: CrossProject): Project = {
      val name = c.jvm.id.stripSuffix("JVM")
      val pr: Seq[ProjectReference] = Seq(c.jvm.project,  c.js.project)

      Project(id = name, base = new File(s"${name}/.prj"))
        .settings(noPublishSettings:_*)
        .settings(projSettings)
        .dependsOn(c.jvm, c.js)
        .aggregate(c.jvm, c.js)
   }

  /**
   * Helper method for mkPrj so that the main project settings need not be repeated for each module
   */
  def mkPrjFactory(projSettings: Seq[sbt.Setting[_]]) = (c: CrossProject) => mkPrj(projSettings, c)

  // Config builders

  /**
   * Helper method that sets the default project(i.e. platform independent), JS and JVM settings.
   */
  def mkConfig(projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
      jsSettings: Seq[sbt.Setting[_]] ): CrossProject ⇒ CrossProject =
    _.settings(projSettings:_*)
    .jsSettings(jsSettings:_*)
    .jvmSettings(jvmSettings:_*)

  /**
   * Helper method that sets the root project's settings
   *
   * In addition to setting the root settings, also adds the rootJVM settings to be used in
   * in root console.
   */
  def mkRootConfig(projSettings: Seq[sbt.Setting[_]] , projJVM:Project): Project ⇒ Project =
    _.in(file("."))
    .settings(rootSettings)
    .settings(projSettings)
    .settings(crossScalaVersions := Nil)
    .settings(console := (console in (projJVM, Compile)).value)

  /**
   * Creates the rootJVM project.
   *
   * Creates the rootJVM project in ".rootJVM" with the default settings and
   * JVM specific default settings.
   */
  def mkRootJvmConfig(s: String, projSettings: Seq[sbt.Setting[_]],
      jvmSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
    .settings(projSettings)
    .settings(jvmSettings)
    .in(file("." + s + "JVM"))


  /**
   * Creates the rootJS project.
   *
   * Creates the rootJS project in ".rootJS" with the default settings and
   * JS specific default settings.
   */
  def mkRootJsConfig(s: String, projSettings: Seq[sbt.Setting[_]],
      jsSettings: Seq[sbt.Setting[_]]): Project ⇒ Project =
    _.settings(moduleName := s)
    .settings(projSettings)
    .settings(jsSettings)
    .in(file("." + s + "JS"))
    .enablePlugins(ScalaJSPlugin)

  import scala.language.postfixOps
  /**
   * Creates a configuration for a document project for scaladoc and a GitHubPages site
   *
   * Creates the basic settings for a document project based on the supplied GitHub settings,
   * project and JVM settings. The documentation is created for the supplied list of projects.
   */
  def mkDocConfig(gh: GitHubSettings, projSettings: Seq[sbt.Setting[_]], jvmSettings: Seq[sbt.Setting[_]],
      deps: Project*): Project ⇒ Project =
    _.settings(projSettings)
    .settings(moduleName := gh.proj + "-docs")
    .settings(noPublishSettings)
    .settings(jvmSettings)
    .dependsOn(deps.map( ClasspathDependency(_, Some("compile;test->test"))):_*)
    .enablePlugins(GhpagesPlugin)
    .enablePlugins(TutPlugin)
    .enablePlugins(ScalaUnidocPlugin)
    .settings(
       organization  := gh.organisation,
       autoAPIMappings := true,
       unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(deps.map(_.project)  :_*),
       docsMappingsAPIDir := "api",
       addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), docsMappingsAPIDir),
       ghpagesNoJekyll := false,
       scalacOptions in Tut --= Seq("-Ywarn-unused-import", "-Ywarn-unused:imports", "-Ywarn-dead-code"),
       scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
         "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
         "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
         "-diagrams"
       ),
       git.remoteRepo := gh.repo,
       includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md")

}

object CatalystsKeys extends BaseCatalystsKeys

class  BaseCatalystsKeys  {

  lazy val travisBuild = settingKey[Boolean]("Build by TravisCI instead of local dev environment")

  lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")
}
