pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri(providers.gradleProperty("sopra-gitlab.package-registry.url").get())
            credentials(HttpHeaderCredentials::class.java) {
                name = "Private-Token"
                value = providers.gradleProperty("sopra-gitlab.package-registry.token").get()
            }
            authentication {
                create("header", HttpHeaderAuthentication::class.java)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "bgw-example-collapsi"
