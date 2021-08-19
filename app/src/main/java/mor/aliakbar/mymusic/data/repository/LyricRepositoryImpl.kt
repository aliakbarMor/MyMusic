package mor.aliakbar.mymusic.data.repository

import kotlinx.coroutines.flow.Flow
import mor.aliakbar.mymusic.data.dataclass.Lyric
import mor.aliakbar.mymusic.data.datasource.LyricDataSource
import javax.inject.Inject

class LyricRepositoryImpl @Inject constructor(private val lyricRemoteSource: LyricDataSource) :
    LyricRepository {

    override fun getLyric(artist: String, title: String): Flow<Lyric> {
        return lyricRemoteSource.getLyric(artist, title)
    }

}