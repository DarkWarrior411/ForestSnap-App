package com.example.forestsnap.di

import android.content.Context
import androidx.room.Room
import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.local.SyncSnapDao
import com.example.forestsnap.data.remote.ForestSnapApi
import com.example.forestsnap.data.remote.WeatherService
import com.example.forestsnap.data.repository.SyncSnapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.forestsnap.core.utils.PreferenceManager
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
        ).build()
    }

    @Provides
    fun provideSyncSnapDao(database: ForestDatabase): SyncSnapDao {
        return database.syncSnapDao()
    }

    @Provides
    @Singleton
    fun provideSyncSnapRepository(database: ForestDatabase, @ApplicationContext context: Context, preferenceManager: PreferenceManager): SyncSnapRepository {
        return SyncSnapRepository(database, context, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideForestSnapApi(): ForestSnapApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Local development server from emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForestSnapApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherService(): WeatherService {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}
