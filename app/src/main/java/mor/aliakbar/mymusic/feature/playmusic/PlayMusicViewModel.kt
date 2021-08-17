package mor.aliakbar.mymusic.feature.playmusic

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.services.musicservice.MusicService
import javax.inject.Inject

@HiltViewModel
class PlayMusicViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    state: SavedStateHandle
) : BaseViewModel() {

    @Inject lateinit var mediaPlayer: MediaPlayer
    var musicsList = MutableLiveData(emptyList<Music>())
    var position: Int = state.get<Int>("position")!!
    var music = MutableLiveData<Music>()

    val toastMassage = MutableLiveData<String>()
    val stateMusic = MutableLiveData<String>()
    val isPlay = MutableLiveData<Boolean>()

    val isShuffle = MutableLiveData(false)
    val isRepeat = MutableLiveData(false)
    val isFavorite = MutableLiveData<Boolean>()
    val currentPositionTime = MutableLiveData(0)

    init {
        viewModelScope.launch {
            val list = async { musicRepository.getCurrentList() }
            musicsList.value = list.await()
            music.value = list.await()[position]

            checkIsFavorite()
            setCurrentPosition()

        }
    }

    private fun checkIsFavorite() {
        viewModelScope.launch {
            val isOnFavorite =
                musicRepository.isMusicInFavorite(music.value!!.title!!, music.value!!.artist!!)
            isFavorite.postValue(isOnFavorite != 0)
        }
    }

    private fun setCurrentPosition() {
        viewModelScope.launch {
            while (true) {
                currentPositionTime.postValue(mediaPlayer.currentPosition)
                delay(100)
            }
        }
    }

    fun skipPrevious() {
        if (position > 0) {
            position--
            music.value = musicsList.value!![position]
        } else {
            position = musicsList.value!!.size - 1
            music.value = musicsList.value!![position]
        }
        music.value = musicsList.value!![position]
        checkIsFavorite()
    }

    fun skipNext() {
        if (position < musicsList.value!!.size - 1) {
            position++
            music.value = musicsList.value!![position]
        } else {
            position = 0
            music.value = musicsList.value!![position]
        }
        music.value = musicsList.value!![position]
        checkIsFavorite()
    }

    fun onShuffleClicked() {
        if (isShuffle.value!!) {
            isShuffle.value = false
            stateMusic.value = MusicService.StateMusic.NORMAL.name
            toastMassage.value = "Shuffle off"
        } else {
            isShuffle.value = true
            stateMusic.value = MusicService.StateMusic.SHUFFLE.name
            if (isRepeat.value!!) {
                isRepeat.postValue(false)
                toastMassage.value = "Shuffle on \nRepeat off"
            } else {
                toastMassage.value = "Shuffle on"
            }
        }
    }

    fun onRepeatClicked() {
        if (isRepeat.value!!) {
            isRepeat.postValue(false)
            stateMusic.value = MusicService.StateMusic.NORMAL.name
            toastMassage.value = "Repeat off"
        } else {
            isRepeat.postValue(true)
            stateMusic.value = MusicService.StateMusic.REPEAT.name
            if (isShuffle.value!!) {
                isShuffle.value = false
                toastMassage.value = "Repeat on \nShuffle off"
            } else {
                toastMassage.value = "Repeat on"
            }
        }
    }

    fun onPauseAndPlayClicked() {
        if (mediaPlayer.isPlaying) {
            isPlay.postValue(false)
        } else {
            isPlay.postValue(true)
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            music.value!!.playListName = "Favorite"
            if (!isFavorite.value!!) {
                isFavorite.postValue(true)
                musicRepository.insertMusic(music.value!!)
                toastMassage.value = "added to favorite"
            } else {
                isFavorite.postValue(false)
                musicRepository.deleteMusic(
                    music.value!!.title!!,
                    music.value!!.artist!!,
                    music.value!!.playListName!!
                )
                toastMassage.value = "remove from favorite"
            }
        }
    }

    fun checkIsNewSong(): Boolean {
        return (music.value?.path != musicRepository.loadLastMusicPlayed().path)
    }


}