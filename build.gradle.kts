plugins {
    `java-library`
}

allprojects {
    apply(plugin = "java")

    group = "net.skeagle"
    version = "6.0.0-beta.1"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        maven("https://repo.skeagle.net/snapshots")
        gradlePluginPortal()
    }

    dependencies {
        compileOnly("net.skeagle:vrnlib:2.2.1")
        compileOnly("com.mojang:authlib:6.0.54")
    }

    tasks.withType<JavaCompile> {
        options.release.set(21)
        options.encoding = "UTF-8"
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}
