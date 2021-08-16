package mor.aliakbar.mymusic.base

import androidx.room.Database
import androidx.room.RoomDatabase
import mor.aliakbar.mymusic.data.datasource.MusicDbSource
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.dataclass.PlayList

@Database(entities = [Music::class,PlayList::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val musicDao: MusicDbSource
//    Todo impl playListDao
//    abstract val playListDao: MusicDbSource


}