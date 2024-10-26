package com.assignment.imagestreaming.di

import android.content.Context
import androidx.room.Room
import com.assignment.imagestreaming.data.local.DataBaseRepository
import com.assignment.imagestreaming.data.local.dao.ImageDao
import com.assignment.imagestreaming.data.local.database.ImageFilesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideImageFilesDao(appDatabase: ImageFilesDatabase): ImageDao {
        return appDatabase.ImagesDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): ImageFilesDatabase {
        return Room.databaseBuilder(
            appContext,
            ImageFilesDatabase::class.java,
            "imagesDatabase"
        ).build()
    }
    @Provides
    @Singleton
    fun provideDataBaseRepository(dao: ImageDao): DataBaseRepository {
        return DataBaseRepository(dao)
    }

}