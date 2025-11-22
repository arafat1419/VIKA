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
                username = providers.gradleProperty("github.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("github.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "VIKA"
include(":sample-app")
include(":VikaSDK")
