package mor.aliakbar.mymusic.data.repository

import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music

interface MusicRepository{

    fun getDeviceMusic(): List<Music>

    suspend fun insertMusic(music: Music)

    suspend fun insertSomeMusics(list: List<Music>)

    suspend fun deleteMusic(title: String, artist: String, playListName: String)

    suspend fun isMusicInFavorite(title: String, artist: String): Music?

    suspend fun getMusicsFromPlaylist(playListName: String): List<Music>

    suspend fun getMostPlayedMusic(): List<Music>

    suspend fun getMusicInMostPlayed(title: String, artist: String): Music?

    suspend fun updateNumberOfPlayed(title: String, artist: String, numberOfPlayedSong: Long): Int

    fun loadLastMusicPlayed(): Music

    fun loadLastIndexMusic(): Int

    fun saveLastMusicPlayed(music: Music, lastMusicIndex: Int)

    fun updateListSateContainer(state: ListStateType)

}