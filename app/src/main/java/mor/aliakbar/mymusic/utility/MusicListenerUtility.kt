package mor.aliakbar.mymusic.utility

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import java.io.File
import java.io.IOException

object MusicListenerUtility {

    fun shareMusic(activity: Activity, uri: String) {
        val intentShare = Intent(Intent.ACTION_SEND)
        val uriParse = Uri.parse(uri)
        intentShare.putExtra(Intent.EXTRA_STREAM, uriParse)
        intentShare.type = "audio/*"
        activity.startActivity(Intent.createChooser(intentShare, "Share Sound File"))
    }

    fun deleteMusic(activity: Activity, filePath: String) {
        val file = File(filePath)
        file.delete()
        if (file.exists()) {
            try {
                file.canonicalFile.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (file.exists()) {
                activity.applicationContext?.deleteFile(file.name)
                if (file.exists()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val cursor: Cursor? =
                            activity.contentResolver?.query(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                arrayOf(
                                    MediaStore.Audio.Media._ID
                                ),
                                MediaStore.Audio.Media.DATA + " =?",
                                arrayOf<String>(file.absolutePath),
                                null
                            )
                        while (cursor!!.moveToNext()) {
                            val id = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media._ID
                                )
                            )
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong()
                            )
                            activity.contentResolver?.delete(uri, null, null)
                        }
                        cursor.close()
                    }
                }
            }
        }

    }

    fun setMusicListState(isMostPlayedList: Boolean) {
        if (isMostPlayedList)
            ListStateContainer.update(ListStateType.MOST_PLAYED)
        else if (ListStateContainer.state != ListStateType.FILTERED &&
            ListStateContainer.state != ListStateType.CUSTOM &&
            ListStateContainer.state != ListStateType.PLAY_LIST
        )
            ListStateContainer.update(ListStateType.DEFAULT)
    }
}