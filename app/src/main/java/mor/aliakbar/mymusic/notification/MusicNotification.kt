package mor.aliakbar.mymusic.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.feature.MainActivity
import mor.aliakbar.mymusic.services.musicservice.MusicService
import javax.inject.Inject

class MusicNotification @Inject constructor(@ApplicationContext private val context: Context) {

    lateinit var notificationManager: NotificationManager
    lateinit var notification: NotificationCompat.Builder
    lateinit var remoteViews: RemoteViews

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannelId(): String {
        val channelId = "music_id"
        NotificationChannel(channelId, "music", NotificationManager.IMPORTANCE_NONE).also {
            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            it.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            it.setSound(null, null)
            notificationManager.createNotificationChannel(it)
        }
        return channelId
    }

    fun createNotification(music: Music, position: Int): Notification? {

        val bundle = Bundle()
        bundle.putInt("position", position)

        val mainPendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.playMusic)
            .setArguments(bundle)
            .createPendingIntent()

        val skipPreviousIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_PREVIOUS
        }
        val skipPreviousPendingIntent =
            PendingIntent.getService(
                context,
                13311,
                skipPreviousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val pauseIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP_AND_RESUME
        }
        val pausePendingIntent =
            PendingIntent.getService(
                context,
                13311,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val skipNextIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_NEXT
        }
        val skipNextPendingIntent =
            PendingIntent.getService(
                context,
                13311,
                skipNextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        remoteViews = RemoteViews(context.packageName, R.layout.notification)
        remoteViews.setTextViewText(R.id.text_title_ntf, music.title)
        remoteViews.setTextViewText(R.id.text_artist_ntf, music.artist)
        remoteViews.setOnClickPendingIntent(R.id.ic_skip_previous_song, skipPreviousPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.ic_play_and_pause_song, pausePendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.ic_skip_next_song, skipNextPendingIntent)

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelId()
        } else ""

        notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setSmallIcon(R.drawable.ic_music)
            .setContentIntent(mainPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(0)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(music.path)
        val art = metaRetriever.embeddedPicture
        if (art != null) {
            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
            notification.setLargeIcon(songImage)
        }
        return notification.build()
    }

}