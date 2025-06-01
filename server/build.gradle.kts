plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(11)

  // Add the results of compiling the frontend project as resources so they can be served by the server.
  sourceSets {
    main {
      resources.srcDir(project(":frontend").getTasksByName("jsBrowserDistribution", false))
    }
  }
}

dependencies {
  // Logging
  implementation("org.jraf:klibnanolog:_")

  // Ktor
  implementation(Ktor.server.core)
  implementation(Ktor.server.netty)
  implementation(Ktor.server.defaultHeaders)
  implementation(Ktor.server.contentNegotiation)
  implementation(Ktor.server.statusPages)
  implementation(Ktor.server.callLogging)
  implementation(Ktor.plugins.serialization.kotlinx.json)

  // Json
  implementation(KotlinX.serialization.json)

  // Receipt
  api(project(":receipt"))
}
