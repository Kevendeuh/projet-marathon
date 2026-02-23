pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // Dépôt pour TVM Runtime (Vital pour le moteur C++)
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }

        // JitPack (Au cas où)
        maven { url = uri("https://jitpack.io") }

        // Ton dossier local libs (Important)
        flatDir {
            dir("mlc4j/src/main/jniLibs")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 1. Dépôt pour les librairies Apache TVM (C'est ici qu'est tvm-runtime)
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://repository.apache.org/content/repositories/snapshots/") }

        // 2. Dépôt officiel MLC
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
