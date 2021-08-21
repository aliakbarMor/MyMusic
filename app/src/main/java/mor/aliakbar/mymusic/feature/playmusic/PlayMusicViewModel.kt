package mor.aliakbar.mymusic.feature.playmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mor.aliakbar.mymusic.base.BaseViewModel
import mor.aliakbar.mymusic.data.dataclass.Lyric
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.LyricRepository
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.services.musicservice.MusicService
import mor.aliakbar.mymusic.utility.Utils.removeNameSiteFromMusic
import javax.inject.Inject

@HiltViewModel
class PlayMusicViewModel @Inject constructor(
    state: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val lyricRepository: LyricRepository,
) : BaseViewModel() {

    @Inject lateinit var mediaPlayer: MediaPlayer
    var musicsList = MutableLiveData(emptyList<Music>())
    var position: Int = state.get<Int>("position")!!
    var music = MutableLiveData<Music>()

    val toastMassage = MutableLiveData<String>()
    val stateMusic = MutableLiveData<String>()
    val isPlay = MutableLiveData(true)

    val isShuffle = MutableLiveData(false)
    val isRepeat = MutableLiveData(false)
    val isFavorite = MutableLiveData<Boolean>()
    val currentPositionTime = MutableLiveData(0)

    init {
        viewModelScope.launch {
            val list = async { musicRepository.getCurrentList() }
            musicsList.value = list.await()
            music.value = list.await()[position]

        }
    }

    fun checkIsFavorite() {
        viewModelScope.launch {
            val isOnFavorite =
                musicRepository.isMusicInFavorite(music.value!!.title!!, music.value!!.artist!!)
            isFavorite.postValue(isOnFavorite != 0)
        }
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

    fun getLyric(): Flow<Lyric> {
        return lyricRepository.getLyric(
            removeNameSiteFromMusic(music.value?.artist!!),
            removeNameSiteFromMusic(music.value?.title!!)
        )
    }

    var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                MusicService.ACTION_MUSIC_COMPLETED -> {
                    position = intent.extras!!.getInt("position")
                    music.value = intent.extras!!.getParcelable<Music>("music")!!
                }
                MusicService.ACTION_STOP_AND_RESUME -> {
                    isPlay.value = intent.extras!!.getBoolean("isPlay")
                }
                MusicService.ACTION_MUSIC_IN_PROGRESS -> {
                    currentPositionTime.postValue(intent.extras!!.getInt("currentPositionTime"))
                }
            }
        }
    }


}