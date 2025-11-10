import edu.udo.cs.sopra.util.addFileToDistribution
import edu.udo.cs.sopra.util.sonatypeSnapshots
import edu.udo.cs.sopra.util.sopraPackageRegistry
import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "2.2.10"
    application
    id("edu.udo.cs.sopra") version "1.0.3"
    kotlin("plugin.serialization") version "2.2.10"
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
    sonatypeSnapshots()
    sopraPackageRegistry()
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

/* This is how you can add the HowToPlay.pdf to the distribution zip file */
addFileToDistribution(file("HowToPlay.pdf"))

/* This is how you can ignore additional classes from test coverage */
/* All classes in gui, entity and service.bot package are already excluded. */

/* To ignore a class Foo in the package foo.bar.baz you would use the following line */
// this.ignoreClassesInCoverageReport("foo.bar.baz.Foo")

/* To ignore all classes in the foo.bar.baz package use a wildcard like this */
// this.ignoreClassesInCoverageReport("foo.bar.baz.*")

tasks.clean {
    delete.add("public")
}
