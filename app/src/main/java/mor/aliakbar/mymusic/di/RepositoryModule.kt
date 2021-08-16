package mor.aliakbar.mymusic.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mor.aliakbar.mymusic.base.AppDatabase
import mor.aliakbar.mymusic.data.datasource.MusicDeviceSource
import mor.aliakbar.mymusic.data.datasource.MusicPreferencesSource
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.data.repository.MusicRepositoryImpl
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

}