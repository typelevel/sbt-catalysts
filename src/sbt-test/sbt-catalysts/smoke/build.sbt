import org.typelevel.libraries
import sbtcrossproject.CrossPlugin.autoImport.CrossType

val apache2 = "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")
val gh = GitHubSettings(org = "kailuowang", proj = "Sheep", publishOrg = "com.kailuowang", license = apache2)


val vAll = libraries
  .addJVM(name = "newtype" , version = "0.1.0", org= "io.estatico")
  .addJVM(name = "scalacheck-shapeless_1.13" , version = "1.1.6", org= "com.github.alexarchambault")
  .add(   name = "http4s" , version = "0.18.0-M8", org = "org.http4s", modules = "http4s-dsl", "http4s-blaze-server", "http4s-blaze-client")




lazy val rootSettings = buildSettings ++ commonSettings ++ publishSettings ++ scoverageSettings
lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
lazy val prj = mkPrjFactory(rootSettings)

lazy val rootJVM = project
  .configure(mkRootJvmConfig(gh.proj, rootSettings, commonJvmSettings))
  .aggregate(coreJVM)
  .settings(noPublishSettings)


lazy val rootJS = project
  .configure(mkRootJsConfig(gh.proj, rootSettings, commonJsSettings))
  .aggregate(coreJS)
  .settings(noPublishSettings)


lazy val core    = prj(coreM)
lazy val coreJVM = coreM.jvm
lazy val coreJS  = coreM.js
lazy val coreM   = module("core", CrossType.Pure)
  .settings(vAll.libDeps("cats-core", "newtype"))
  .settings(vAll.testLibDeps("scalacheck-shapeless_1.13", "scalatest"))
  .settings(simulacrumSettings(vAll, false))



lazy val buildSettings = sharedBuildSettings(gh, vAll)

lazy val commonSettings = sharedCommonSettings ++ scalacAllSettings ++ Seq(
  developers := List(Developer("Kailuo Wang", "@kailuowang", "kailuo.wang@gmail.com", new java.net.URL("http://kailuowang.com"))),

  parallelExecution in Test := false,
  crossScalaVersions := Seq(vAll.vers("scalac_2.11"), scalaVersion.value),
  scalacOptions in Test ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code")))
)  ++ unidocCommonSettings ++
  addCompilerPlugins(vAll, "kind-projector")

lazy val commonJsSettings = Seq(scalaJSStage in Global := FastOptStage)

lazy val commonJvmSettings = Seq()

lazy val publishSettings = sharedPublishSettings(gh) ++ credentialSettings ++ sharedReleaseProcess

lazy val scoverageSettings = sharedScoverageSettings(60)

lazy val disciplineDependencies = addLibs(vAll, "discipline", "scalacheck")
