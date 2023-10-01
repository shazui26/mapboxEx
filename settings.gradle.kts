pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        maven {
            url= uri ("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
                password = "sk.eyJ1IjoiY3JpY2tldDI3IiwiYSI6ImNsbWhoYmtjdzJicHMzbHB4MDVsaGJ3YjIifQ.vVnhM840-lJ0Y0OZ8dNN_g"
            }
        }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url= uri ("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = "mapbox"
                // Use the secret token you stored in gradle.properties as the password
                password = "sk.eyJ1IjoiY3JpY2tldDI3IiwiYSI6ImNsbWhoYmtjdzJicHMzbHB4MDVsaGJ3YjIifQ.vVnhM840-lJ0Y0OZ8dNN_g"
            }
        }

    }
}

rootProject.name = "mapboxEx"
include(":app")
