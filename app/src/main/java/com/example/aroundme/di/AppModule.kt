package com.example.aroundme.di

import android.content.Context
import androidx.room.Room
import com.example.aroundme.database.PostDao
import com.example.aroundme.database.PostDatabase
import com.example.aroundme.database.UserDao
import com.example.aroundme.database.UserDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 🔥 Firestore
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    // 🗂 Post Database + DAO
    @Provides
    @Singleton
    fun providePostDatabase(@ApplicationContext context: Context): PostDatabase =
        Room.databaseBuilder(context, PostDatabase::class.java, "post_db").build()

    @Provides
    fun providePostDao(postDb: PostDatabase): PostDao = postDb.postDao()

    // 👤 User Database + DAO
    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase =
        Room.databaseBuilder(context, UserDatabase::class.java, "user_db").build()

    @Provides
    fun provideUserDao(userDb: UserDatabase): UserDao = userDb.userDao()
}
