package mor.aliakbar.mymusic.data.datasource

import android.content.SharedPreferences
import mor.aliakbar.mymusic.data.dataclass.Music
import javax.inject.Inject

class MusicPreferencesSource @Inject constructor(private var sharedPreferences: SharedPreferences) :
    MusicDataSource {

    override fun getDeviceMusic(refresh: Boolean): List<Music> {
        TODO("Not yet implemented")
    }

    override suspend fun insertMusic(music: Music) {
        TODO("Not yet implemented")
    }

    override suspend fun insertSomeMusics(list: List<Music>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMusic(title: String, artist: String, playListName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun isMusicInFavorite(
        title: String,
        artist: String,
        playListName: String
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getMusicsFromPlaylist(playlistName: String): List<Music> {
        TODO("Not yet implemented")
    }

    override suspend fun getMusicsByNumberOfPlayed(): List<Music> {
        TODO("Not yet implemented")
    }

    override suspend fun getNumberOfPlayed(
        title: String,
        artist: String,
        playlistName: String
    ): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun updateNumberOfPlayed(
        title: String,
        artist: String,
        numberOfPlayedSong: Long,
        playlistName: String
    ): Int {
        TODO("Not yet implemented")
    }

    override fun loadLastMusicPlayed(): Music {
        return Music(
            null,
            sharedPreferences.getString("artist", null),
            sharedPreferences.getString("title", null),
            sharedPreferences.getString("path", null),
            sharedPreferences.getString("duration", null)
        )
    }

    override fun loadLastIndexMusic(): Int {
        return sharedPreferences.getInt("position", 0)
    }

    override fun saveLastMusicPlayed(music: Music, lastMusicIndex: Int) {
        sharedPreferences.edit().apply {
            putString("artist", music.artist).apply()
            putString("title", music.title).apply()
            putString("duration", music.duration).apply()
            putString("path", music.path).apply()
            putInt("position", lastMusicIndex).apply()
        }

    }


}