// inspired by Spire build.sbt file

val scala210Version = "2.10.6"
val scala211Version = "2.11.8"
val disciplineVersion = "0.4"
val scalaCheckVersion = "1.12.4"
val scalaMacrosVersion = "2.0.1"
val scalaTestVersion = "3.0.0-M7"
val shapelessVersion = "2.2.5"
val spireVersion = "0.11.0"

lazy val scalin = (project in file("."))
  .settings(moduleName := "scalin")
  .settings(scalinSettings: _*)
  .settings(noPublishSettings)
  .aggregate(macros, core)
  .dependsOn(macros, core)

lazy val macros = (project in file("macros"))
  .settings(moduleName := "scalin-macros")
  .settings(scalinSettings: _*)
  .settings(commonJvmSettings: _*)

lazy val core = (project in file("core"))
  .settings(moduleName := "scalin-core")
  .settings(scalinSettings: _*)
  .settings(scalaTestSettings: _*)
  .settings(libraryDependencies += "org.scalacheck" %% "scalacheck" % scalaCheckVersion)
  .settings(commonJvmSettings: _*)
  .dependsOn(macros)

lazy val scalinSettings = buildSettings ++ commonSettings ++ publishSettings ++ tutSettings

lazy val buildSettings = Seq(
  organization := "net.alasc",
  scalaVersion := scala211Version,
  crossScalaVersions := Seq(scala210Version, scala211Version)
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.diff(Seq(
    "-Xfatal-warnings", 
    "-language:existentials",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )),
  resolvers ++= Seq(
    "bintray/non" at "http://dl.bintray.com/non/maven",
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies += "org.spire-math" %% "spire" % spireVersion
) ++ scalaMacroDependencies ++ warnUnusedImport

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/denisrosset/scalin")),
  licenses += ("GPL-3.0", url("http://opensource.org/licenses/GPL-3.0")),
  bintrayRepository := "maven",
  publishArtifact in Test := false
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
) ++ selectiveOptimize
  // -optimize has no effect in scala-js other than slowing down the build

// do not optimize on Scala 2.10 because of optimizer bugs (cargo-cult setting
// from my experience with metal)
lazy val selectiveOptimize = 
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-optimize")
    }
  }

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console))
)

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
      // in Scala 2.10, quasiquotes are provided by macro paradise
      case Some((2, 10)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full),
              "org.scalamacros" %% "quasiquotes" % scalaMacrosVersion cross CrossVersion.binary
        )
    }
  }
)

lazy val scalaTestSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "com.chuusai" %% "shapeless" % shapelessVersion % "test",
    "org.typelevel" %% "discipline" % disciplineVersion % "test",
    "org.spire-math" %% "spire-laws" % spireVersion % "test"
  )
)
