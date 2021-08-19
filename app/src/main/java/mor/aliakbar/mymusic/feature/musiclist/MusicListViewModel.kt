package mor.aliakbar.mymusic.feature.musiclist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.dataclass.PlayList
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.data.repository.PlayListRepository
import mor.aliakbar.mymusic.services.musicservice.MusicService
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playListRepository: PlayListRepository,
    var state: SavedStateHandle
) : BaseViewModel() {

    var currentList = MutableLiveData<List<Music>>()
    var musicListHorizontal = MutableLiveData<List<Music>>()

    var lastMusicPlayed = MutableLiveData<Music>()
    var percentageTime = MutableLiveData<Int>()

    var playLists = MutableLiveData<ArrayList<PlayList>>()
    var isInPlayList = MutableLiveData(false)

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
        viewModelScope.launch {
            val playlistName = state.get<String>("playlistName")!!
            if (playlistName != "mainMusicList")
                ListStateContainer.update(ListStateType.PLAY_LIST)

            currentList.value = musicRepository.getCurrentList(playlistName)
            isInPlayList.value = playlistName != "mainMusicList"

            val list = musicRepository.getMostPlayedMusic()
            if (list.size < 2) {
                musicListHorizontal.value = currentList.value
            } else
                musicListHorizontal.value = list
        }

        lastMusicPlayed.value = musicRepository.loadLastMusicPlayed()
    }

    fun saveLastMusicPlayed(music: Music) {
        musicRepository.saveLastMusicPlayed(music)
        lastMusicPlayed.value = music
    }

    fun getCurrentSongIndex(): Int? =
        currentList.value?.indexOfFirst { music -> music.path == lastMusicPlayed.value?.path }

    fun updatePercentageCurrentPositionTime(time: Int) {
        percentageTime.value = time
    }

    fun insertPlaylist(playlist: PlayList) {
        viewModelScope.launch {
            playListRepository.insertPlaylist(playlist)
        }
        playLists.value = playLists.value?.apply { add(playlist) }
    }

    fun addMusicToPlayList(music: Music) =
        viewModelScope.launch { musicRepository.insertMusic(music) }

    fun insertSomeMusics(list: List<Music>) =
        viewModelScope.launch { musicRepository.insertSomeMusics(list) }

    fun playingNextSong(position: Int) {
        ListStateContainer.update(ListStateType.CUSTOM)

        val currentSongIndex = currentList.value!!.indexOf(lastMusicPlayed.value)
        musicRepository.customList.value?.add(
            currentSongIndex + 1,
            currentList.value!![position]
        )
        if (currentSongIndex < position) {
            musicRepository.customList.value?.removeAt(position + 1)
        } else
            musicRepository.customList.value?.removeAt(position)
    }

    fun search(text: String) {
        val filteredMusic = musicRepository.getDeviceMusic().filter {
            it.title!!.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT)) ||
                    it.artist!!.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault()))
        }
        if (text != "")
            ListStateContainer.update(ListStateType.FILTERED)
        else
            ListStateContainer.update(ListStateType.DEFAULT)

        musicRepository.filteredList.value = filteredMusic
        currentList.value = filteredMusic
    }

    var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        val eh = CoroutineExceptionHandler { _, e -> Log.d("exception handler:", "$e") }
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val bundle = intent.extras
            when (action) {
                MusicService.ACTION_MUSIC_STARTED -> {
                    CoroutineScope(Dispatchers.Main + eh).launch {
                        val music = bundle!!.getParcelable<Music>("music")!!
                        saveLastMusicPlayed(music)
                    }
                }
                MusicService.ACTION_MUSIC_IN_PROGRESS -> {
                    updatePercentageCurrentPositionTime(
                        bundle!!.getInt("currentPositionTime") * 100
                                / lastMusicPlayed.value?.duration!!.toInt()
                    )
                }
            }
        }
    }


}