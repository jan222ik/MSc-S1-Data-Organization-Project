

plugins {
    kotlin("multiplatform") // kotlin("jvm") doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
}

kotlin {

    jvm {
        withJava()
    }

    sourceSets {
        named("jvmMain") {

            dependencies {

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("org.litote.kmongo:kmongo-coroutine:4.2.3")
                implementation("io.lettuce:lettuce-core:6.0.1.RELEASE")
                implementation("com.google.code.gson:gson:2.8.6")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }




}



