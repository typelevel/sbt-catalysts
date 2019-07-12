# sbt-catalysts

[![Join the chat at https://gitter.im/typelevel/sbt-catalysts](https://badges.gitter.im/typelevel/sbt-catalysts.svg)](https://gitter.im/typelevel/sbt-catalysts?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Sbt plugin that provides a common build infrastructure and library dependencies for typelevel-like and 
similar cross platform projects. 

## Usage

To use it, just add it to your `project/plugins.sbt`:

```scala
addSbtPlugin("org.typelevel" % "sbt-catalysts" % LATEST_VERSION)
```

This will automatically:

- Include other plugins - see [the build file for the current list](https://github.com/typelevel/sbt-catalysts/blob/master/build.sbt#L13-L25)
- Provide versions and library settings used by Typelevel and related projects - see [Dependencies](https://github.com/typelevel/sbt-catalysts/blob/master/src/main/scala/org/typelevel/package.scala), which is automatically updated by the awesome [scala-steward](https://github.com/fthomas/scala-steward/). 
- Include some functionality to help keep your build file sane, especially if you are cross building against multiple Scala versions and Scala.JS - see the quick example below


## Overview

It is very important to realise that using the plugin *does not actually change the
the build in any way, but merely provides the methods to help create a build.sbt.** (except the plugins) 
The design goal was thus to facilitate setting up a build rather than providing an build template.  

Compared to a plugin that "does everything", the advantage of this approach is that it is far
easier to use other functionality where appropriate and also to "see" what the build is
is actually doing, as the methods are (mainly) just **pure SBT methods**. 

sbt-catalyst provides a helper library to manage library dependencies. 
The norm would be:

In a small project, define the library dependencies explicitly:

```scala
libraryDependencies += "org.typelevel" %% "alleycats-core" %  "0.1.0"
```

If another sub-project also has this dependency, it's common to move the definition to a val, and
often the version too. It the library is used in another module for test only, another val is 
required:

```scala
val alleycatsV = "0.1.0"
val alleycatsDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats-core" % alleycatsV)
val alleycatsTestDeps = Seq(libraryDependencies += "org.typelevel" %% "alleycats-laws" % alleycatsV % "test")
```

Whilst this works fine for individual projects, it's not ideal when a group of loosely coupled want
to share a common set (or sets) of dependencies, that they can also modify locally if required.
In this sense, we need "cascading configuration dependency files" and this is what this plugin also
provides. It's also a lot of repeated settings when you have multiple modules

sbt-catalyst provides a "Libraries" class to help manage library dependencies. In the following 
example, `org.typelevel.libraries` is a built-in `Versions` that included over [two dozens of typelevel libraries](https://github.com/typelevel/sbt-catalysts/blob/master/src/main/scala/org/typelevel/package.scala).  
Then you can easily add your own libraries in a concise way or override versions. 
```scala
val libs = org.typelevel.libraries
  .add   (name = "cats" ,    version = "2.0.0-M4") //override versions
  .addJVM(name = "newtype" , version = "0.1.0", org= "io.estatico") //add a JVM only lib
  .addJava(name= "commons-maths3" , version = "3.6.1", org= "org.apache.commons") //add a java only lib
  .addJVM(name = "scalacheck-shapeless_1.13" , version = "1.1.6", org= "com.github.alexarchambault")
  .add   (name = "http4s" , version = "0.18.0-M8", org = "org.http4s", modules = "http4s-dsl", "http4s-blaze-server", "http4s-blaze-client")
```
Then later when defining your module, you can use the `dependencies` method to add, well, dependencies. 
```scala
.settings(
    libs.dependencies("cats-core", "cats-collections", "newtype", "http4s-blaze-client", "commons-maths3"),
    libs.testDependencies("scalacheck-shapeless_1.13", "scalatest"))
```


## Example of cross building project using sbt-catalyst

In this example build file, we used most of the methods provided by sbt-catalyst. In real project, you can pick whatever that works for you.   
We define a project that:
- Has a root project configured as an umbrella project for JVM/JS builds for multiple scala versions
- Has two sub-projects(modules) that cross compile to JVM and JS
- Use the standard typelevel set of dependencies and versions
- Provides best-practice scalac options
- Publishes regular snapshots as well as git-versioned immutable snapshots
- Sets the root project's console settings to include the required dependencies
- Includes Scoverage test coverage metrics
- Configured for automated release to Sonatype OSS release and snapshot repositories
- Generates scaladoc documentation and a GithubPages web site

```scala


addCommandAlias("gitSnapshots", ";set version in ThisBuild := git.gitDescribedVersion.value.get + \"-SNAPSHOT\"")

/**
 * Project settings
 */
val gh = GitHubSettings(org = "InTheNow", proj = "catalysts", publishOrg = "org.typelevel", license = apache)
val libs = org.typelevel.libraries.add("cats", "1.3.0") //override cats version

/**
 * Root - This is the root project that aggregates the catalystsJVM and catalystsJS sub projects
 */
lazy val rootSettings = buildSettings ++ commonSettings ++ publishSettings ++ scoverageSettings
lazy val module = mkModuleFactory(gh.proj, mkConfig(rootSettings, commonJvmSettings, commonJsSettings))
lazy val prj = mkPrjFactory(rootSettings)

lazy val rootPrj = project
  .configure(mkRootConfig(rootSettings,rootJVM))
  .aggregate(rootJVM, rootJS)
  .dependsOn(rootJVM, rootJS)

lazy val rootJVM = project
  .configure(mkRootJvmConfig(gh.proj, rootSettings, commonJvmSettings))
  .aggregate(macrosJVM, platformJVM, docs)
  .dependsOn(macrosJVM, platformJVM)

lazy val rootJS = project
  .configure(mkRootJsConfig(gh.proj, rootSettings, commonJsSettings))
  .aggregate(macrosJS, platformJS)

/** Macros - cross project that defines macros.*/
lazy val macros    = prj(macrosM)
lazy val macrosJVM = macrosM.jvm
lazy val macrosJS  = macrosM.js
lazy val macrosM   = module("macros", CrossType.Pure)
  .settings(macroCompatSettings(vAll):_*)

/** Platform - cross project that provides cross platform support.*/
lazy val platform    = prj(platformM)
lazy val platformJVM = platformM.jvm
lazy val platformJS  = platformM.js
lazy val platformM   = module("platform", CrossType.Dummy)
  .dependsOn(macrosM)
  .settings(
    libs.dependencies("specs2-core","specs2-scalacheck"),
    libs.testDependencies("scalatest")
  )

/** Docs - Generates and publishes the scaladoc API documents and the project web site.*/
lazy val docs = project.configure(mkDocConfig(gh, rootSettings, commonJvmSettings,
    platformJVM, macrosJVM))

/** Settings.*/
lazy val buildSettings = sharedBuildSettings(gh, vAll)

lazy val commonSettings = sharedCommonSettings ++ Seq(
  parallelExecution in Test := false,
  developers := List(Developer("Kailuo Wang", "@kailuowang", "kailuo.wang@gmail.com", new java.net.URL("http://kailuowang.com")))
) ++ scalacAllSettings ++ unidocCommonSettings

lazy val commonJsSettings = Seq(scalaJSStage in Global := FastOptStage)

lazy val commonJvmSettings = Seq()
  
lazy val publishSettings = sharedPublishSettings(gh, devs) ++ credentialSettings ++ sharedReleaseProcess

lazy val scoverageSettings = sharedScoverageSettings(60)
```


## Projects using sbt-catalysts

+ [cats-tagless][cats-tagless]
+ [catalysts][catalysts]
+ [henkan][henkan]

### Maintainers

The current maintainers (people who can merge pull requests) are:

 * [adelbertc](https://github.com/adelbertc) Adelbert Chang
 * [ochrons](https://github.com/ochrons) Otto Chrons
 * [BennyHill](https://github.com/BennyHill) Alistair Johnson
 * [non](https://github.com/non) Erik Osheim
 * [milessabin](https://github.com/milessabin) Miles Sabin
 * [fthomas](https://github.com/fthomas) Frank S. Thomas
 * [julien-truffaut](https://github.com/julien-truffaut) Julien Truffaut
 * [dwijnand](https://github.com/dwijnand) Dale Wijnand
 * [kailuowang](https://github.com/kailuowang) Kailuo Wang
 * [andyscott](https://github.com/andyscott) Andy Scott

 
We are currently following a practice of requiring at least two
sign-offs to merge PRs (and for large or contentious issues we may
wait for more). For typos or other small fixes to documentation we
relax this to a single sign-off.

### Contributing

Discussion around sbt-catalysts is currently happening in the
gitter channel, issue and PR pages.

Feel free to open an issue if you notice a bug, have an idea for a
feature, or have a question about the code. Pull requests are also
gladly accepted.

People are expected to follow the
[Scala Code of Conduct](https://typelevel.org/code-of-conduct.html) when
discussing Catalysts on the Github page, Gitter channel, or other
venues.

We hope that our community will be respectful, helpful, and kind. If
you find yourself embroiled in a situation that becomes heated, or
that fails to live up to our expectations, you should disengage and
contact one of the [project maintainers](#maintainers) in private. We
hope to avoid letting minor aggressions and misunderstandings escalate
into larger problems.

If you are being harassed, please contact one of [us](#maintainers)
immediately so that we can support you.

### License

Catalysts is licensed under the **[Apache License, Version 2.0][apache]** (the
"License"); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[apache]: https://www.apache.org/licenses/LICENSE-2.0
[catalysts]: https://github.com/typelevel/catalysts
[cats-tagless]: https://github.com/typelevel/cats-tagless
[henkan]: https://github.com/kailuowang/henkan
