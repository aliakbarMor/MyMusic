package mor.aliakbar.mymusic.feature.musiclist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(private val musicRepository: MusicRepository) :
    BaseViewModel() {

    var musicList = MutableLiveData<List<Music>>()
    var musicListHorizontal = MutableLiveData<List<Music>>()
    var lastMusicPlayed = MutableLiveData<Music>()

    init {
        getMusics()
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

    fun saveLastMusicPlayed(music: Music, position: Int) {
        musicRepository.saveLastMusicPlayed(music, position)
        lastMusicPlayed.value = music
    }


}