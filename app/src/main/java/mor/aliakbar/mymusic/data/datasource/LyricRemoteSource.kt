package mor.aliakbar.mymusic.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import mor.aliakbar.mymusic.data.api.ApiService
import mor.aliakbar.mymusic.data.dataclass.Lyric
import javax.inject.Inject

class LyricRemoteSource @Inject constructor(private val apiService: ApiService) : LyricDataSource {

    override fun getLyric(artist: String, title: String): Flow<Lyric> {
        return flow {
            emit(apiService.getLyric("v1/$artist/$title"))
        }.flowOn(Dispatchers.IO)
    }
}