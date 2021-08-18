package mor.aliakbar.mymusic.services.musicservice

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
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

        const val ACTION_STOP = "action stop"
        const val ACTION_PLAY = "action play"
        const val ACTION_RESUME = "action resume"
        const val ACTION_CHANGE_STATE = "action change state"
    }

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var mediaPlayer: MediaPlayer
    @Inject lateinit var musicNotification: MusicNotification

    private var musicsList = emptyList<Music>()
    private lateinit var music: Music
    private var position: Int = -1

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
                getMusicsList(intent)
                getCurrentMusic(intent)

                startForeground(5, musicNotification.createNotification(music, position))
                playMusic(-1)

                setOnCompletionListener()
            }
            ACTION_STOP -> {
                stopForeground(false)
                mediaPlayer.stop()
                updateIconPlayAndPauseNotification()
            }
            ACTION_RESUME -> {
                startForeground(5, musicNotification.createNotification(music, position))
                playMusic(intent.getIntExtra("currentPositionTime", -1))
                updateIconPlayAndPauseNotification()
            }
            ACTION_CHANGE_STATE -> {
                state = intent.getStringExtra("change state")!!
            }
        }

        return START_STICKY
    }

    private fun updateIconPlayAndPauseNotification() {
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
    }

    private fun setOnCompletionListener() {
        mediaPlayer.setOnCompletionListener {
            if (mediaPlayer.currentPosition - music.duration!!.toInt() < 1000 &&
                mediaPlayer.currentPosition - music.duration!!.toInt() > -1000
            ) {
                when (state) {
                    StateMusic.REPEAT.name -> playMusic(-1)
                    StateMusic.SHUFFLE.name -> {
                        val rand = Random()
                        position = rand.nextInt(musicsList.size - 1)
                        music = musicsList[position]
                        playMusic(-1)
                    }
                    else -> {
                        if (position < musicsList.size - 1) {
                            position++
                            music = musicsList[position]
                            playMusic(-1)
                        } else {
                            position = 0
                            music = musicsList[position]
                            playMusic(-1)
                        }
                    }
                }
                sendBroadcast(
                    ACTION_MUSIC_COMPLETED, Bundle().apply { putInt("currentPosition", position) }
                )
            }
        }
    }

    private fun getCurrentMusic(intent: Intent) {
        position = intent.getIntExtra("position", -1)
        music = musicsList[position]
    }

    private fun getMusicsList(intent: Intent) {
        runBlocking {
            musicsList = musicRepository.getCurrentList()
        }
    }

    private fun playMusic(currentPositionTime: Int) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(music.path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            if (currentPositionTime != -1)
                mediaPlayer.seekTo(currentPositionTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateNumberOfPlayed(music)
        sendBroadcastsStartAndProgress(position)
    }

    private fun sendBroadcastsStartAndProgress(position: Int) {
        val bundle = Bundle()
        bundle.putInt("currentPosition", position)
        sendBroadcast(ACTION_MUSIC_STARTED, bundle)

        CoroutineScope(Dispatchers.IO).launch {
            while (mediaPlayer.duration == music.duration?.toInt()) {
                delay(1000)
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    val bundle1 = Bundle()
                    bundle1.putInt("currentPositionTime", currentPosition)
                    sendBroadcast(ACTION_MUSIC_IN_PROGRESS, bundle1)
                }
            }
        }
    }

    private fun sendBroadcast(action: String, bundle: Bundle) {
        val intent = Intent(action)
        intent.putExtras(bundle)
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

    enum class StateMusic {
        NORMAL,
        REPEAT,
        SHUFFLE
    }
}


