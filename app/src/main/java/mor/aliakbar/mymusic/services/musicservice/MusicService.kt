package mor.aliakbar.mymusic.services.musicservice

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.notification.MusicNotification
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    companion object {
        const val ACTION_MUSIC_STARTED = "com.example.mymusic.action.MUSIC_STARTED"
        const val ACTION_MUSIC_COMPLETED = "com.example.mymusic.action.MUSIC_COMPLETED"
        const val ACTION_MUSIC_IN_PROGRESS = "com.example.mymusic.action.MUSIC_IN_PROGRESS"

        const val ACTION_STOP = "action stop"
        const val ACTION_PLAY = "action play"
    }

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var mediaPlayer: MediaPlayer
    @Inject lateinit var musicNotification: MusicNotification

    private var musicsList = emptyList<Music>()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(false)
            }
            ACTION_PLAY -> {
                runBlocking {
                    musicsList = when (ListStateContainer.state) {
                        ListStateType.DEFAULT -> musicRepository.getDeviceMusic()
                        ListStateType.MOST_PLAYED -> musicRepository.getMostPlayedMusic()
                        ListStateType.PLAY_LIST ->
                            musicRepository.getMusicsFromPlaylist(intent.getStringExtra("playlistName")!!)
//                TODO
                        ListStateType.FILTERED -> musicRepository.getDeviceMusic()
                        ListStateType.CUSTOM -> musicRepository.getDeviceMusic()
                    }
                }

                val position = intent.getIntExtra("position", -1)
                val music = musicsList[position]
                startForeground(
                    5, musicNotification.createNotification(music, position)
                )
                playMusic(music, intent.getIntExtra("currentPositionTime", -1))
                sendBroadcasts(position, music)
                updateNumberOfPlayed(music)
            }
        }

        return START_STICKY
    }

    private fun playMusic(music: Music, currentPositionTime: Int) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(music.path)
        mediaPlayer.prepare()
        mediaPlayer.start()
        if (currentPositionTime != -1)
            mediaPlayer.seekTo(currentPositionTime)
    }

    private fun sendBroadcasts(position: Int, music: Music) {
        val bundle = Bundle()
        bundle.putInt("currentPosition", position)
        sendBroadcast(ACTION_MUSIC_STARTED, bundle)

        val totalTimeSecond = music.duration!!.toInt() / 1000
        CoroutineScope(Dispatchers.IO).launch {
            for (int in 0..totalTimeSecond) {
                delay(1000)
                val currentPosition = mediaPlayer.currentPosition
                val bundle1 = Bundle()
                bundle1.putInt("currentPositionTime", currentPosition)
                sendBroadcast(ACTION_MUSIC_IN_PROGRESS, bundle1)
            }
        }
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

    private fun sendBroadcast(action: String, bundle: Bundle) {
        val intent = Intent(action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


}
