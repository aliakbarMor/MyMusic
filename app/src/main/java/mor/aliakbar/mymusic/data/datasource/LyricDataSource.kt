package mor.aliakbar.mymusic.data.datasource

import kotlinx.coroutines.flow.Flow
import mor.aliakbar.mymusic.data.dataclass.Lyric

interface LyricDataSource {

    fun getLyric(artist: String, title: String): Flow<Lyric>

}