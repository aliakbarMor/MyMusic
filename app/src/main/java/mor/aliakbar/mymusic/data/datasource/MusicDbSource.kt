package mor.aliakbar.mymusic.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mor.aliakbar.mymusic.data.dataclass.Music

@Dao
interface MusicDbSource : MusicDataSource {

    override fun getDeviceMusic(refresh: Boolean): List<Music> {
        TODO("Not yet implemented")
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertMusic(music: Music)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertSomeMusics(list: List<Music>)

    @Query("DELETE FROM musics WHERE title = :title and artist = :artist and playListName = :playListName")
    override suspend fun deleteMusic(title: String, artist: String, playListName: String)

    @Query("SELECT * FROM musics WHERE title = :title and artist = :artist and playListName= :playListName")
    override suspend fun isMusicInFavorite(
        title: String,
        artist: String,
        playListName: String
    ): Music

    @Query("SELECT numberOfPlayedSong FROM musics WHERE title = :title and artist = :artist and playListName = :playlistName")
    override suspend fun getNumberOfPlayed(
        title: String, artist: String, playlistName: String
    ): Long?

    @Query("SELECT * FROM musics WHERE playListName = :playlistName")
    override suspend fun getMusicsFromPlaylist(playlistName: String): List<Music>

    @Query("SELECT * FROM musics ORDER BY numberOfPlayedSong DESC")
    override suspend fun getMusicsByNumberOfPlayed(): List<Music>

    @Query("UPDATE musics SET numberOfPlayedSong = :numberOfPlayedSong WHERE title = :title and artist = :artist and playListName = :playlistName")
    override suspend fun updateNumberOfPlayed(
        title: String, artist: String, numberOfPlayedSong: Long, playlistName: String
    ): Int


    override fun loadLastMusicPlayed(): Music {
        TODO("Not yet implemented")
    }

    override fun loadLastIndexMusic(): Int {
        TODO("Not yet implemented")
    }

    override fun saveLastMusicPlayed(music: Music, lastMusicIndex: Int) {
        TODO("Not yet implemented")
    }
}