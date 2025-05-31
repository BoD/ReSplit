plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(11)
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
