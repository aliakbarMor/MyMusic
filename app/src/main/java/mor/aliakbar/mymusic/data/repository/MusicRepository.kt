package mor.aliakbar.mymusic.data.repository

import androidx.lifecycle.MutableLiveData
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music

interface MusicRepository {

    var customList: MutableLiveData<ArrayList<Music>>

    var filteredList: MutableLiveData<List<Music>>

    suspend fun getCurrentList(playListName: String? = null): List<Music>

    fun getDeviceMusic(): List<Music>

    suspend fun insertMusic(music: Music)

    suspend fun insertSomeMusics(list: List<Music>)

    suspend fun deleteMusic(title: String, artist: String, playListName: String)

    suspend fun isMusicInFavorite(title: String, artist: String): Int

    suspend fun getMusicsFromPlaylist(playListName: String): List<Music>

    suspend fun getMostPlayedMusic(): List<Music>

    suspend fun getNumberOfPlayed(title: String, artist: String): Long?

    suspend fun updateNumberOfPlayed(title: String, artist: String, numberOfPlayedSong: Long): Int

    fun loadLastMusicPlayed(): Music

    fun saveLastMusicPlayed(music: Music)

}