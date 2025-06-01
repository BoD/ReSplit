plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  kotlin("plugin.compose")
}

kotlin {
  js {
    browser()
    binaries.executable()
    compilerOptions {
      target.set("es2015")
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        // Logging
        implementation("org.jraf:klibnanolog:_")

        // Compose
        implementation(compose.html.core)
        implementation(compose.runtime)

        // Coroutines
        implementation(KotlinX.coroutines.core)

        // BigNum
        implementation("com.ionspin.kotlin:bignum:_")

        // Receipt
        implementation(project(":receipt"))
      }
    }
  }
}
