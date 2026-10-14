plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.tapmoc)
}

kotlin {
  // Add the results of compiling the frontend project as resources so they can be served by the server.
  sourceSets {
    main {
      resources.srcDir(project(":frontend").getTasksByName("jsBrowserDistribution", false))
    }
  }
}

dependencies {
  // Logging
  implementation(libs.klibnanolog)

  // Ktor
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.default.headers)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.serialization.kotlinx.json)

  // Json
  implementation(libs.kotlinx.serialization.json)

  // Receipt
  api(project(":receipt"))
}

tapmoc {
  java(25)
  kotlin(libs.versions.kotlin.get())
  checkDependencies()
}
