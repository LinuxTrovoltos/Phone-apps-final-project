package com.example.aroundme.utils

import com.example.aroundme.database.PostEntity
import com.example.aroundme.models.Post

fun PostEntity.toDomain(): Post = Post(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
    creatorId = creatorId,
    timestamp = timestamp,
    category = category
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
    creatorId = creatorId,
    timestamp = timestamp,
    category = category,
    isSynced = true // New posts default to unsynced
)
