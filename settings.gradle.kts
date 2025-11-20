import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // GitHub Packages - VIKA SDK
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/arafat1419/VIKA")
            credentials {
                // Read from local.properties
                val localProperties = Properties()
                val localPropertiesFile = File(rootDir, "local.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use { stream ->
                        localProperties.load(stream)
                    }
                }

                username =
                    localProperties.getProperty("github.user") ?: System.getenv("GITHUB_ACTOR")
                password =
                    localProperties.getProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "VIKA"
include(":sample-app")
include(":VikaSDK")
