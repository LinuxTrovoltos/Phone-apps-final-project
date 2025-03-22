package com.example.aroundme.models

data class Post(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val creatorId: String,
    val timestamp: Long,
    val category: String = "general"
)
