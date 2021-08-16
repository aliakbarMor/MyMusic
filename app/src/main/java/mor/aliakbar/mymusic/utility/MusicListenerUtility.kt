package mor.aliakbar.mymusic.utility

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import mor.aliakbar.mymusic.R
import java.io.File
import java.io.IOException

object MusicListenerUtility {

    fun createPopUpMenu(activity: Activity, view: View): PopupMenu {
        return PopupMenu(activity.applicationContext, view).apply {
            menuInflater.inflate(R.menu.subject_menu, menu)
        }
    }

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
                    Thread(Runnable {
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
                    })
                }
            }
        }

    }
}