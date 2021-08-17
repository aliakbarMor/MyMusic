package mor.aliakbar.mymusic.feature.playlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    var state: SavedStateHandle
) :
    ViewModel() {

    var musicList = MutableLiveData<List<Music>>()

    init {
        val playlistName = state.get<String>("playlistName")!!
        viewModelScope.launch {
            musicList.value = musicRepository.getMusicsFromPlaylist(playlistName)

        }
    }
}