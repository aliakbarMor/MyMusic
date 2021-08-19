package mor.aliakbar.mymusic.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mor.aliakbar.mymusic.base.AppDatabase
import mor.aliakbar.mymusic.data.api.ApiService
import mor.aliakbar.mymusic.data.datasource.LyricRemoteSource
import mor.aliakbar.mymusic.data.datasource.MusicDeviceSource
import mor.aliakbar.mymusic.data.datasource.MusicPreferencesSource
import mor.aliakbar.mymusic.data.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        @ApplicationContext context: Context,
        appDatabase: AppDatabase
    ): MusicRepository {
        return MusicRepositoryImpl(
            MusicDeviceSource(context),
            appDatabase.musicDao,
            MusicPreferencesSource(context.getSharedPreferences("app", Application.MODE_PRIVATE))
        )
    }

    @Provides
    fun providePlayListRepository(appDatabase: AppDatabase): PlayListRepository {
        return PlayListRepositoryImpl(appDatabase.playListDao)
    }

    @Provides
    fun provideLyricRepository(apiService: ApiService): LyricRepository {
        return LyricRepositoryImpl(LyricRemoteSource(apiService))
    }

}