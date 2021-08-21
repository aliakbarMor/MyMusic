package mor.aliakbar.mymusic.services.musicservice

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.notification.MusicNotification
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    companion object {
        var isServiceStart: Boolean = false

        const val ACTION_MUSIC_STARTED = "com.example.mymusic.action.MUSIC_STARTED"
        const val ACTION_MUSIC_COMPLETED = "com.example.mymusic.action.MUSIC_COMPLETED"
        const val ACTION_MUSIC_IN_PROGRESS = "com.example.mymusic.action.MUSIC_IN_PROGRESS"

        const val ACTION_STOP_AND_RESUME = "com.example.mymusic.action.ACTION_STOP_AND_RESUME"
        const val ACTION_PLAY = "com.example.mymusic.action.ACTION_PLAY"
        const val ACTION_SKIP_NEXT = "com.example.mymusic.action.ACTION_SKIP_NEXT"
        const val ACTION_SKIP_PREVIOUS = "com.example.mymusic.action.ACTION_SKIP_PREVIOUS"
        const val ACTION_CHANGE_STATE = "com.example.mymusic.action.ACTION_CHANGE_STATE"
        const val ACTION_SEEK_TO = "com.example.mymusic.action.ACTION_SEEK_TO"
    }

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var mediaPlayer: MediaPlayer
    @Inject lateinit var musicNotification: MusicNotification

    private var musicsList = emptyList<Music>()
    private lateinit var music: Music
    private var position: Int = -1
    private var cacheCurrentPosition: Int = 0

    var state: String = StateMusic.NORMAL.name

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        isServiceStart = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY -> {
                cacheCurrentPosition = -1
                getMusicsList()
                getCurrentMusic(intent)

                playMusic()
                setOnCompletionListener()
            }
            ACTION_STOP_AND_RESUME -> {
                if (mediaPlayer.isPlaying) {
                    cacheCurrentPosition = mediaPlayer.currentPosition
                    stopForeground(false)
                    mediaPlayer.stop()
                } else
                    playMusic()
                updateIconPlayAndPause()
            }
            ACTION_CHANGE_STATE -> {
                state = intent.getStringExtra("change state")!!
            }
            ACTION_SKIP_NEXT -> {
                cacheCurrentPosition = -1
                skipNext()
            }
            ACTION_SKIP_PREVIOUS -> {
                cacheCurrentPosition = -1
                skipPrevious()
            }
            ACTION_SEEK_TO -> {
                val seekTo = intent.getIntExtra("seek to", -1)
                if (mediaPlayer.isPlaying) mediaPlayer.seekTo(seekTo)
                else cacheCurrentPosition = seekTo
            }
        }

        return START_STICKY
    }

    private fun updateIconPlayAndPause() {
        if (mediaPlayer.isPlaying) {
            musicNotification.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_pause
            )
        } else {
            musicNotification.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_play
            )
        }
        musicNotification.notificationManager.notify(5, musicNotification.notification.build())

        sendBroadcast(ACTION_STOP_AND_RESUME, Bundle().apply {
            putBoolean("isPlay", mediaPlayer.isPlaying)
        })
    }

    private fun setOnCompletionListener() {
        mediaPlayer.setOnCompletionListener {
            if (mediaPlayer.currentPosition - music.duration!!.toInt() < 1000 &&
                mediaPlayer.currentPosition - music.duration!!.toInt() > -1000
            ) {
                when (state) {
                    StateMusic.REPEAT.name -> playMusic()
                    else -> skipNext()
                }
                sendBroadcast(ACTION_MUSIC_COMPLETED, Bundle().apply {
                    putInt("position", position)
                    putParcelable("music", music)
                })
            }
        }
    }

    private fun getCurrentMusic(intent: Intent) {
        position = intent.getIntExtra("position", -1)
        music = musicsList[position]
    }

    private fun getMusicsList() {
        runBlocking {
            musicsList = musicRepository.getCurrentList()
        }
    }

    private fun playMusic() {
        startForeground(5, musicNotification.createNotification(music, position))
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(music.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            if (cacheCurrentPosition != -1)
                mediaPlayer.seekTo(cacheCurrentPosition)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateNumberOfPlayed(music)
        sendBroadcastsStartAndProgress()
    }

    private fun sendBroadcastsStartAndProgress() {
        sendBroadcast(ACTION_MUSIC_STARTED, Bundle().apply { putParcelable("music", music) })

        CoroutineScope(Dispatchers.IO).launch {
            while (mediaPlayer.duration == music.duration?.toInt()) {
                delay(500)
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    sendBroadcast(ACTION_MUSIC_IN_PROGRESS, Bundle().apply {
                        putInt("currentPositionTime", currentPosition)
                    })
                }
            }
        }
    }

    private fun sendBroadcast(action: String, bundle: Bundle) {
        val intent = Intent(action).apply {
            putExtras(bundle)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun updateNumberOfPlayed(music: Music) {
        CoroutineScope(Dispatchers.IO).launch {
            val numberOfPlayed = musicRepository.getNumberOfPlayed(music.title!!, music.artist!!)
            if (numberOfPlayed == null) {
                music.playListName = "most played"
                music.numberOfPlayedSong = 1
                musicRepository.insertMusic(music)
            } else {
                musicRepository.updateNumberOfPlayed(
                    music.title!!,
                    music.artist!!,
                    numberOfPlayed + 1
                )
            }
        }
    }

    private fun skipPrevious() {
        if (position > 0) {
            position--
            music = musicsList[position]
        } else {
            position = musicsList.size - 1
            music = musicsList[position]
        }
        music = musicsList[position]
        playMusic()
        sendBroadcast(ACTION_MUSIC_COMPLETED, Bundle().apply {
            putInt("position", position)
            putParcelable("music", music)
        })
    }

    private fun skipNext() {
        if (state == StateMusic.SHUFFLE.name) {
            val rand = Random()
            position = rand.nextInt(musicsList.size - 1)
            music = musicsList[position]
            playMusic()
        } else {
            if (position < musicsList.size - 1) {
                position++
                music = musicsList[position]
            } else {
                position = 0
                music = musicsList[position]
            }
            music = musicsList[position]
            playMusic()
        }
        sendBroadcast(ACTION_MUSIC_COMPLETED, Bundle().apply {
            putInt("position", position)
            putParcelable("music", music)
        })
    }

    enum class StateMusic {
        NORMAL,
        REPEAT,
        SHUFFLE
    }
}


