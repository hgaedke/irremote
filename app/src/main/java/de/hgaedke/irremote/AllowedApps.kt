package de.hgaedke.irremote

import kotlinx.serialization.Serializable

@Serializable
enum class AllowedApps (val app: String) {
    radio1("radio1"),
    radio2("radio2"),
    music(""),
    video(""),
}