plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false
}