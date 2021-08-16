package mor.aliakbar.mymusic.feature.playmusic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class PlayMusicViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    state: SavedStateHandle
) : BaseViewModel() {

    var musicsList = MutableLiveData<List<Music>>()
    var position: Int = state.get<Int>("position")!!
    var music = MutableLiveData<Music>()

    val isShuffle = MutableLiveData(false)
    val isRepeat = MutableLiveData(false)
    val isPlay = MutableLiveData(true)
    val isFavorite = MutableLiveData<Boolean>()
    val currentPositionTime = MutableLiveData(0)

    init {
        viewModelScope.launch {
            val list = async {
                when (ListStateContainer.state) {
                    ListStateType.DEFAULT -> musicRepository.getDeviceMusic()
                    ListStateType.MOST_PLAYED -> musicRepository.getMostPlayedMusic()
                    ListStateType.PLAY_LIST ->
                        musicRepository.getMusicsFromPlaylist(state.get<String>("playlistName")!!)
//                TODO
                    ListStateType.FILTERED -> musicRepository.getDeviceMusic()
                    ListStateType.CUSTOM -> musicRepository.getDeviceMusic()
                }
            }
            musicsList.value = list.await()
            music.value = list.await()[position]
        }
    }

}