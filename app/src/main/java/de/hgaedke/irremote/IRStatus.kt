package de.hgaedke.irremote

import kotlinx.serialization.Serializable

@Serializable
data class IRStatus (
    val app: AllowedApps,
    val radio1_station: Int,
    val radio2_station: Int,
    val music: MusicState,
    val video: VideoState,
)