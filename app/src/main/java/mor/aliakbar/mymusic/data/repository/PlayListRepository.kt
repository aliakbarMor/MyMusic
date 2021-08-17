package mor.aliakbar.mymusic.data.repository

import mor.aliakbar.mymusic.data.dataclass.PlayList

interface PlayListRepository {

    suspend fun insertPlaylist(playList: PlayList)

    suspend fun deletePlayList(playList: PlayList): Int

    suspend fun getPlayListById(id: Int): PlayList

    suspend fun getAll(): List<PlayList>

}