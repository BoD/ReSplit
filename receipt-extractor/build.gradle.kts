plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(11)
}

dependencies {
  // Logging
  implementation(libs.klibnanolog)

  // OpenAI
  implementation(libs.openai.java)

  // Receipt
  api(project(":receipt"))
}
