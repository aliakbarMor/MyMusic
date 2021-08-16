package mor.aliakbar.mymusic.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mor.aliakbar.mymusic.services.loadingimage.GlideLoadingImage
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices

@Module
@InstallIn(SingletonComponent::class)
abstract class BinderModule {

    @Binds
    abstract fun bindGlideLoadingImage(glideLoadingImage: GlideLoadingImage): LoadingImageServices

}