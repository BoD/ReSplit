plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

kotlin {
  jvmToolchain(11)
}

dependencies {
  // Logging
  implementation("org.jraf:klibnanolog:_")

  // OpenAI
  implementation("com.openai:openai-java:_")

  // Receipt
  api(project(":receipt"))
}
