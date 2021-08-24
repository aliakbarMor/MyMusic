package mor.aliakbar.mymusic.feature.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.repository.MusicRepository
import mor.aliakbar.mymusic.services.musicservice.MusicService
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseWidget : AppWidgetProvider() {

    @Inject
    lateinit var repository: MusicRepository
    var music: Music? = null
    var isPlay: Boolean = false

    fun updateMusicWidget(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, views: RemoteViews
    ) {
        music.let { music ->
            views.setTextViewText(R.id.widget_artist, music!!.artist)
            views.setTextViewText(R.id.widget_title, music.title)

            val metaRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(music.path)
            val art = metaRetriever.embeddedPicture
            if (art != null) {
                val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                views.setImageViewBitmap(R.id.widget_image, songImage)
            } else
                views.setImageViewResource(R.id.widget_image, R.drawable.ic_music)

            setOnClickListeners(context, views)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateIcPlayAndPause(
        appWidgetManager: AppWidgetManager, appWidgetId: Int, views: RemoteViews
    ) {
        if (isPlay) {
            views.setImageViewResource(R.id.widget_btn_toggle_play_pause, R.drawable.ic_pause)
        } else
            views.setImageViewResource(R.id.widget_btn_toggle_play_pause, R.drawable.ic_play)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setOnClickListeners(context: Context, views: RemoteViews) {
        val skipPreviousIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_PREVIOUS
        }
        val skipPreviousPendingIntent = PendingIntent.getService(
            context, 13311, skipPreviousIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_btn_prev, skipPreviousPendingIntent)

        val playAndPauseIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP_AND_RESUME
        }
        val playAndPausePendingIntent = PendingIntent.getService(
            context, 13311, playAndPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_btn_toggle_play_pause, playAndPausePendingIntent)

        val skipNextIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SKIP_NEXT
        }
        val skipNextPendingIntent = PendingIntent.getService(
            context, 13311, skipNextIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_btn_next, skipNextPendingIntent)
    }

}