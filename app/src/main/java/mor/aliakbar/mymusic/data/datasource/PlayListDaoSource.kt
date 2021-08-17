package mor.aliakbar.mymusic.data.datasource

import androidx.room.*
import mor.aliakbar.mymusic.data.dataclass.PlayList

@Dao
interface PlayListDaoSource : PlayListDataSource {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertPlaylist(playList: PlayList)

    @Delete
    override suspend fun deletePlayList(playList: PlayList): Int

    @Query("SELECT * FROM playLists WHERE id = :id")
    override suspend fun getPlayListById(id: Int): PlayList

    @Query("SELECT * FROM playLists")
    override suspend fun getAll(): List<PlayList>
}
