plugins {
    kotlin("multiplatform") // kotlin("jvm") doesn't work well in IDEA/AndroidStudio (https://github.com/JetBrains/compose-jb/issues/22)
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)

                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.materialIconsExtended)
                api(compose.ui)

                /*
                // Ktor Server & REST & Websocket
                val ktorVersion = "1.3.0"
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")

                // Logger
                val logbackVersion = "1.2.3"
                implementation("ch.qos.logback:logback-classic:$logbackVersion")

                // Database
                val exposedVersion = "0.26.2"
                val h2Version = "1.4.200"
                val hikariCpVersion = "3.4.5"
                implementation("com.h2database:h2:$h2Version")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("com.zaxxer:HikariCP:$hikariCpVersion")

                 */
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            //targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
        }
    }

}
