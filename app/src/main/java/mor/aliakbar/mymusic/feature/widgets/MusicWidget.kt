package mor.aliakbar.mymusic.feature.widgets

import android.appwidget.AppWidgetManager
import android.content.*
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.services.musicservice.MusicService


@AndroidEntryPoint
class MusicWidget : BaseWidget() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        if (music == null)
            music = repository.loadLastMusicPlayed()
        for (appWidgetId in appWidgetIds) {
            updateMusicWidget(
                context,
                appWidgetManager,
                appWidgetId,
                RemoteViews(context.packageName, R.layout.music_widget)
            )
        }
    }

    override fun onEnabled(context: Context) {
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_STARTED)
        musicIntentFilter.addAction(MusicService.ACTION_STOP_AND_RESUME)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(musicReceiver, musicIntentFilter)
    }

    override fun onDisabled(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(musicReceiver)
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val remoteViews = RemoteViews(context.packageName, R.layout.music_widget)
            when (intent.action) {
                MusicService.ACTION_MUSIC_STARTED -> {
                    music = intent.extras!!.getParcelable<Music>("music")!!
                    isPlay = true

                    val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(context, MusicWidget::class.java)
                    )
                    for (appWidgetId in appWidgetIds) {
                        this@MusicWidget.updateMusicWidget(
                            context, AppWidgetManager.getInstance(context), appWidgetId, remoteViews
                        )
                        this@MusicWidget.updateIcPlayAndPause(
                            AppWidgetManager.getInstance(context), appWidgetId, remoteViews
                        )
                    }
                }
                MusicService.ACTION_STOP_AND_RESUME -> {
                    isPlay = intent.extras!!.getBoolean("isPlay")
                    val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        ComponentName(context, MusicWidget::class.java)
                    )
                    for (appWidgetId in appWidgetIds) {
                        this@MusicWidget.updateIcPlayAndPause(
                            AppWidgetManager.getInstance(context), appWidgetId, remoteViews
                        )
                    }
                }
            }
        }
    }
}