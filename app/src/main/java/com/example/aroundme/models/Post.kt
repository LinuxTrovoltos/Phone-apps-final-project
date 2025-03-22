package com.example.aroundme.models

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val creatorId: String = "",
    val timestamp: Long = 0L,
    val category: String = "general"
)
