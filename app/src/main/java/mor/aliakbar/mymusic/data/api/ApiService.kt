package mor.aliakbar.mymusic.data.api

import mor.aliakbar.mymusic.data.dataclass.Lyric
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {

    @GET
    fun getLyric(@Url url: String): Lyric

}