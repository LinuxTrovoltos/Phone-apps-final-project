package com.example.aroundme.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val category: String = "General",       // Optional: filter support
    val isSynced: Boolean = false,          // Local-first sync flag
    val updatedAt: Long = System.currentTimeMillis()  // Conflict resolution
)
