package com.dong.focusflow.di

import android.content.Context
import androidx.room.Room
import com.dong.focusflow.data.local.AppDatabase
import com.dong.focusflow.data.local.dao.PomodoroDao
import com.dong.focusflow.data.local.datastore.SettingsDataStore
import com.dong.focusflow.data.repository.PomodoroRepository
import com.dong.focusflow.utils.SoundPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pomodoro_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePomodoroDao(database: AppDatabase): PomodoroDao {
        return database.pomodoroDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun providePomodoroRepository(
        pomodoroDao: PomodoroDao,
        settingsDataStore: SettingsDataStore
    ): PomodoroRepository {
        return PomodoroRepository(pomodoroDao, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideSoundPlayer(@ApplicationContext context: Context): SoundPlayer {
        return SoundPlayer(context)
    }
}