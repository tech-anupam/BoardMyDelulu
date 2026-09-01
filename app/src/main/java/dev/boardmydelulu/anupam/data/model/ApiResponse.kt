package dev.boardmydelulu.anupam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val status: Int,
    val author: String,
    val data: T? = null,
    val message: String? = null
)
