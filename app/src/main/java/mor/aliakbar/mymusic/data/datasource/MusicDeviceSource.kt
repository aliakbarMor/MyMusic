package mor.aliakbar.mymusic.data.datasource

import android.content.Context
import android.provider.MediaStore
import android.text.TextUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import mor.aliakbar.mymusic.data.dataclass.Music
import javax.inject.Inject

class MusicDeviceSource @Inject constructor(@ApplicationContext private val context: Context) :
    MusicDataSource {

    var cacheList = emptyList<Music>()

    override fun getDeviceMusic(refresh: Boolean): List<Music> {
        if (cacheList.isEmpty()) {
            val list = ArrayList<Music>()
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.AudioColumns.IS_MUSIC
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val music = Music(
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    )
                    if (music.title!!.contains("-")) {
                        val str = music.title!!.split("-")
                        if (TextUtils.isEmpty(music.artist))
                            music.artist = str[0]
                        if (TextUtils.isEmpty(music.title))
                            music.title = str[1]
                    }
                    if (music.duration != null) {
                        list.add(music)
                    }
                }
                cursor.close()
            }
            list.sortByDescending { it.musicId }
            cacheList = list
            return list
        } else return cacheList
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
        TODO("Not yet implemented")
    }

    override fun loadLastIndexMusic(): Int {
        TODO("Not yet implemented")
    }

    override fun saveLastMusicPlayed(music: Music) {
        TODO("Not yet implemented")
    }

}