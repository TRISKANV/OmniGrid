package com.cyber.omnigrid.feature.automation.domain.model

data class Payload(
    val id: String,
    val name: String,
    val description: String,
    val content: String,
    val isFavorite: Boolean
)
