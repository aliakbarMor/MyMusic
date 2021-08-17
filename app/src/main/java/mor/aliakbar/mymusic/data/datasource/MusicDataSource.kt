package mor.aliakbar.mymusic.data.datasource

import mor.aliakbar.mymusic.data.dataclass.Music

interface MusicDataSource {

    fun getDeviceMusic(refresh: Boolean = false): List<Music>

    suspend fun insertMusic(music: Music)

    suspend fun insertSomeMusics(list: List<Music>)

    suspend fun deleteMusic(title: String, artist: String, playListName: String)

    suspend fun isMusicInFavorite(
        title: String, artist: String, playListName: String = "Favorite"
    ): Int

    suspend fun getMusicsFromPlaylist(playlistName: String): List<Music>

    suspend fun getMusicsByNumberOfPlayed(): List<Music>

    suspend fun getNumberOfPlayed(
        title: String, artist: String, playlistName: String = "most played"
    ): Long?

    suspend fun updateNumberOfPlayed(
        title: String,
        artist: String,
        numberOfPlayedSong: Long,
        playlistName: String = "most played"
    ): Int

    fun loadLastMusicPlayed(): Music

    fun loadLastIndexMusic(): Int

    fun saveLastMusicPlayed(music: Music)

}