pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "VRNCore"

include("api")
include("plugin")
include("nms-1_21_R1")
include("nms-1_21_R2")
include("nms-1_21_R3")
include("nms-1_21_R4")
include("nms-1_21_R5")
