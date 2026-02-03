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
        // 1. Dépôt pour les librairies Apache TVM (C'est ici qu'est tvm-runtime)
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://repository.apache.org/content/repositories/snapshots/") }

        // Dépôt MLC
        maven { url = uri("https://maven.pkg.dev/mlc-ai/mlc-llm") }
        // 2. Dépôt officiel MLC (Gardez-le, il est vivant)
        maven { url = uri("https://maven.pkg.dev/mlc-ai/mlc-llm") }

        // 3. Dépôt JitPack (Souvent utilisé en secours)
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
        flatDir{
            dir("mlc4j/src/main/jniLibs")}


    }
}

rootProject.name = "PllRun"
include(":app")
include(":pllrunwatch")
include(":mlc4j")
