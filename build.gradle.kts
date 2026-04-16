import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "2.2.10"
    application
    kotlin("plugin.serialization") version "2.2.10"
    id("org.jetbrains.dokka") version "2.2.0"
    id("dev.detekt") version "2.0.0-alpha.2"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"

}

group = "edu.udo.cs.sopra"
version = "1.0"

/* Change this to the version of the BGW you want to use */
val bgwVersion = "0.10"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Library for testing.
    testImplementation(kotlin("test-junit5"))

    // BGW dependencies.
    implementation(group = "tools.aqua", name = "bgw-gui", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-common", version = bgwVersion)
    implementation(group = "tools.aqua", name = "bgw-net-client", version = bgwVersion)

    // Saving/Loading.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Playing .ogg files.
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    implementation("com.googlecode.soundlibs:tritonus-share:0.3.7.4")
    implementation("com.googlecode.soundlibs:jorbis:0.0.17.4")
}

tasks.clean {
    delete.add("public")
}

tasks.distZip {
    archiveFileName.set("distribution.zip")
    destinationDirectory.set(layout.projectDirectory.dir("public"))
}

tasks.test {
    useJUnitPlatform()
    reports.html.outputLocation.set(layout.projectDirectory.dir("public/test"))
    finalizedBy(tasks.koverHtmlReport)
    ignoreFailures = true
}

kover {
    reports {
        filters {
            excludes {
                classes("gui.*", "entity.*", "*MainKt*", "service.bot.*")
            }
        }

        total {
            xml {
                xmlFile.set(file("public/coverage/report.xml"))
            }
            html {
                htmlDir.set(layout.projectDirectory.dir("public/coverage"))
            }
        }
    }
}

detekt {
    toolVersion = "2.0.0-alpha.2"
    config.from("detektConfig.yml")
}

tasks.detektMain {
    reports.html.outputLocation.set(file("public/detekt/main.html"))
}

tasks.detektTest {
    reports.html.outputLocation.set(file("public/detekt/test.html"))
}

tasks.dokkaHtml.configure {
    outputDirectory.set(projectDir.resolve("public/dokka"))
}

