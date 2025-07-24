import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    application
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.18.0-RC3"
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "edu.udo.cs.sopra"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(group = "tools.aqua", name = "bgw-core", version = "0.2")
}

tasks.distZip {
    archiveFileName.set("distribution.zip")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(listOf("view/**"))
        }
    }))
}

detekt {
    // Version of Detekt that will be used. When unspecified the latest detekt
    // version found will be used. Override to stay on the same version.
    toolVersion = "1.18.0-RC3"

    //source.setFrom()
    config = files("detektConfig.yml")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
