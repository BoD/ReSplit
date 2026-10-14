plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.tapmoc)
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
        implementation(libs.klibnanolog)

        // Compose
        implementation(libs.compose.html)
        implementation(libs.compose.runtime)

        // Coroutines
        implementation(libs.kotlinx.coroutines.core)

        // BigNum
        implementation(libs.bignum)

        // Receipt
        implementation(project(":receipt"))
      }
    }
  }
}

tapmoc {
  java(25)
  kotlin(libs.versions.kotlin.get())
  checkDependencies()
}
