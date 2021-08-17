package mor.aliakbar.mymusic.feature.musiclist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.dataclass.PlayList
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.data.repository.PlayListRepository
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playListRepository: PlayListRepository
) :
    BaseViewModel() {

    var musicList = MutableLiveData<List<Music>>()
    var musicListHorizontal = MutableLiveData<List<Music>>()

    var lastMusicPlayed = MutableLiveData<Music>()
    var percentageTime = MutableLiveData<Int>()

    var playLists = MutableLiveData<ArrayList<PlayList>>()

    init {
        getMusics()
        getPlayLists()
    }

    private fun getPlayLists() {
        viewModelScope.launch {
            playLists.value = playListRepository.getAll() as ArrayList<PlayList>
        }
    }

    private fun getMusics() {
        musicList.value = musicRepository.getDeviceMusic()

        viewModelScope.launch {
            val list = musicRepository.getMostPlayedMusic()
            if (list.size < 2) {
                musicListHorizontal.value = musicList.value
            } else
                musicListHorizontal.value = list
        }

        lastMusicPlayed.value = musicRepository.loadLastMusicPlayed()
    }

    fun updateListSateContainer(state: ListStateType) {
        musicRepository.updateListSateContainer(state)
    }

    suspend fun getCurrentMusicList(): List<Music> {
        return musicRepository.getCurrentList()
    }

    fun saveLastMusicPlayed(music: Music) {
        musicRepository.saveLastMusicPlayed(music)
        lastMusicPlayed.value = music
    }

    fun getCurrentSongIndex(): Int? {
        return musicList.value?.indexOfFirst { music ->
            music.path == lastMusicPlayed.value?.path
        }
    }

    fun updatePercentageCurrentPositionTime(time: Int) {
        percentageTime.value = time
    }

    fun insertPlaylist(playlist: PlayList) {
        viewModelScope.launch {
            playListRepository.insertPlaylist(playlist)
        }
        playLists.value = playLists.value?.apply { add(playlist) }
    }


}