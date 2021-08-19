package mor.aliakbar.mymusic.data.repository

import kotlinx.coroutines.flow.Flow
import mor.aliakbar.mymusic.data.dataclass.Lyric

interface LyricRepository {

    fun getLyric(artist: String, title: String): Flow<Lyric>

}