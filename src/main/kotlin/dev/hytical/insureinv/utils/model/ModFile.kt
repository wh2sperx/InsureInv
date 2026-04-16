package dev.hytical.insureinv.utils.model

import kotlinx.serialization.Serializable

@Serializable
data class ModFile(
    val url: String,
    val filename: String,
    val primary: Boolean = true
)
