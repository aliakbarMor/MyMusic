package mor.aliakbar.mymusic.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.datasource.MusicDataSource
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val musicDeviceSource: MusicDataSource,
    private val musicDbSource: MusicDataSource,
    private val musicPreferencesSource: MusicDataSource
) : MusicRepository {

    override fun getDeviceMusic(): List<Music> {
        return musicDeviceSource.getDeviceMusic()
    }

    override suspend fun insertMusic(music: Music) {
        musicDbSource.insertMusic(music)
    }

    override suspend fun insertSomeMusics(list: List<Music>) {
        musicDbSource.insertSomeMusics(list)
    }

    override suspend fun deleteMusic(title: String, artist: String, playListName: String) {
        musicDbSource.deleteMusic(title, artist, playListName)
    }

    override suspend fun isMusicInFavorite(title: String, artist: String): Music? {
        return musicDbSource.isMusicInFavorite(title, artist)
    }

    override suspend fun getMusicsFromPlaylist(playListName: String): List<Music> {
        return musicDbSource.getMusicsFromPlaylist(playListName)
    }

    override suspend fun getMostPlayedMusic(): List<Music> {
        return withContext(Dispatchers.IO) { musicDbSource.getMusicsByNumberOfPlayed() }
    }

    override suspend fun getMusicInMostPlayed(title: String, artist: String): Music? {
        return musicDbSource.getMusicInMostPlayed(title, artist)
    }

    override suspend fun updateNumberOfPlayed(
        title: String, artist: String, numberOfPlayedSong: Long
    ): Int {
        return musicDbSource.updateNumberOfPlayed(title, artist, numberOfPlayedSong)
    }

    override fun loadLastMusicPlayed(): Music {
        return musicPreferencesSource.loadLastMusicPlayed()
    }

    override fun loadLastIndexMusic(): Int {
        return musicPreferencesSource.loadLastIndexMusic()
    }

    override fun saveLastMusicPlayed(music: Music, lastMusicIndex: Int) {
        musicPreferencesSource.saveLastMusicPlayed(music, lastMusicIndex)
    }

    override fun updateListSateContainer(state: ListStateType) {
        ListStateContainer.update(state)
    }
}