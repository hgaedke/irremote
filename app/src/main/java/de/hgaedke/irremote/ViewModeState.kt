package de.hgaedke.irremote

import kotlinx.serialization.Serializable

@Serializable
enum class ViewModeState (val state: String) {
    VIEW_MODE_FOLDER("VIEW_MODE_FOLDER"),
    VIEW_MODE_PLAYBACK("VIEW_MODE_PLAYBACK"),
}