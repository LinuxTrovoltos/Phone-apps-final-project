package com.example.aroundme.database

import com.example.aroundme.models.Post

data class PostWithUser(
    val post: Post,
    val user: UserEntity?
)