package dev.hytical.insureinv.utils.model

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersion(
    val versionNumber: String,
    val files: List<ModFile>,
    val gameVersions: List<String>,
    val versionType: String,
    val datePublished: String
)