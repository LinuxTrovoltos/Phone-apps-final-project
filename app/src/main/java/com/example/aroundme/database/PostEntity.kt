package com.example.aroundme.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val creatorId: String,
    val timestamp: Long,
    val category: String?,
    val isSynced: Boolean = false
)
