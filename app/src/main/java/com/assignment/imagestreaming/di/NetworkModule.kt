package com.assignment.imagestreaming.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.assignment.imagestreaming.data.network.FileUploadRepository
import com.assignment.imagestreaming.data.network.FileUploadRepositoryImpl
import com.assignment.imagestreaming.data.service.FileUploadService
import com.assignment.imagestreaming.services.ImageUploadWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://file.io/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFileUploadService(retrofit: Retrofit): FileUploadService {
        return retrofit.create(FileUploadService::class.java)
    }

    @Provides
    @Singleton
    fun provideFileUploadRepository(fileUploadService: FileUploadService): FileUploadRepository {
        return FileUploadRepositoryImpl(fileUploadService)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(provideWorkerFactory(context))
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkerFactory(
        @ApplicationContext context: Context,
    ): WorkerFactory {
        return ImageUploadWorkerFactory(provideFileUploadService(provideRetrofit(provideOkHttpClient())))
    }

}