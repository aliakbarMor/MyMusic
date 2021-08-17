package mor.aliakbar.mymusic.data.datasource

import mor.aliakbar.mymusic.data.dataclass.PlayList

interface PlayListDataSource {

    suspend fun insertPlaylist(playList: PlayList)

    suspend fun deletePlayList(playList: PlayList): Int

    suspend fun getPlayListById(id: Int): PlayList

    suspend fun getAll(): List<PlayList>


}