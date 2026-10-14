plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.tapmoc)
}

dependencies {
  // Logging
  implementation(libs.klibnanolog)

  // OpenAI
  implementation(libs.openai.java)

  // Receipt
  api(project(":receipt"))
}

tapmoc {
  java(25)
  kotlin(libs.versions.kotlin.get())
  checkDependencies()
}
