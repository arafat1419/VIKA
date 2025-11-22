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

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/arafat1419/VIKA")
            credentials {
                val localProperties = java.util.Properties()
                val localPropertiesFile = File(rootDir, "local.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use { localProperties.load(it) }
                }
                username = localProperties.getProperty("github.user") ?: System.getenv("GITHUB_ACTOR")
                password = localProperties.getProperty("github.token") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "VIKA"
include(":sample-app")
include(":VikaSDK")
