plugins {
    id("io.github.openminigameserver.arcadiumgradle") version "1.0-SNAPSHOT"
}

nickarcade {
    name = "Discord"
    depends("Chat", "Moderation")
}

repositories {
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    api("net.dv8tion:JDA:4.2.0_251") {
        exclude(module = "opus-java")
    }
    api("cloud.commandframework:cloud-jda:1.4.0")
}

tasks {
    shadowJar {
        dependencies {
            this.exclude(dependency("com.squareup.okhttp3:okhttp"))
            this.exclude(dependency("com.fasterxml.jackson.core:"))
            this.exclude(dependency("com.squareup.okio:"))
            this.exclude(dependency("cloud.commandframework:cloud-core:"))
            this.exclude(dependency("io.leangen.geantyref:"))
            this.exclude(dependency(":cloud-services:"))
        }
    }
}