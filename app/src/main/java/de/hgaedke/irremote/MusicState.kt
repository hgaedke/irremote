package de.hgaedke.irremote

import kotlinx.serialization.Serializable

@Serializable
data class MusicState (
    val viewMode: ViewModeState,
    val albumName: String,
    val currentSongName: String,
)