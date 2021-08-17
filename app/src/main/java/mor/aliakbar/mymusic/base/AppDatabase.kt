package mor.aliakbar.mymusic.base

import androidx.room.Database
import androidx.room.RoomDatabase
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.dataclass.PlayList
import mor.aliakbar.mymusic.data.datasource.MusicDaoSource
import mor.aliakbar.mymusic.data.datasource.PlayListDaoSource

@Database(entities = [Music::class, PlayList::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val musicDao: MusicDaoSource
    abstract val playListDao: PlayListDaoSource

}