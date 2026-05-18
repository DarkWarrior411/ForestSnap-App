package com.example.forestsnap.di

import android.content.Context
import androidx.room.Room
import com.example.forestsnap.core.utils.PreferenceManager
import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.local.SyncSnapDao
import com.example.forestsnap.data.remote.ForestSnapApi
import com.example.forestsnap.data.repository.SyncSnapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient // NEW
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // NEW
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun provideForestDatabase(@ApplicationContext context: Context): ForestDatabase {
        return Room.databaseBuilder(
            context,
            ForestDatabase::class.java,
            "forest_database"
        )
            .addMigrations(ForestDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideSyncSnapDao(database: ForestDatabase): SyncSnapDao {
        return database.syncSnapDao()
    }

    @Provides
    @Singleton
    fun provideSyncSnapRepository(
        database: ForestDatabase,
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): SyncSnapRepository {
        return SyncSnapRepository(database, context, preferenceManager)
    }

    // NEW: Provide the OkHttpClient explicitly for the SSE Stream
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Standard timeouts for normal REST calls
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideForestSnapApi(okHttpClient: OkHttpClient): ForestSnapApi {
        return Retrofit.Builder()
            .baseUrl("http://10.20.34.117:8000/")
            .client(okHttpClient) // FIXED: Pass the provided client to Retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForestSnapApi::class.java)
    }
}