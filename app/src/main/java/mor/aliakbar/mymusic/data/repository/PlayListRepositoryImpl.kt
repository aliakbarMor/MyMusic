package mor.aliakbar.mymusic.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mor.aliakbar.mymusic.data.dataclass.PlayList
import mor.aliakbar.mymusic.data.datasource.PlayListDataSource
import javax.inject.Inject

class PlayListRepositoryImpl @Inject constructor(private val playListDaoSource: PlayListDataSource) :
    PlayListRepository {

    override suspend fun insertPlaylist(playList: PlayList) {
        withContext(Dispatchers.IO) { playListDaoSource.insertPlaylist(playList) }
    }

    override suspend fun deletePlayList(playList: PlayList): Int {
        return withContext(Dispatchers.IO) { playListDaoSource.deletePlayList(playList) }
    }

    override suspend fun getPlayListById(id: Int): PlayList {
        return withContext(Dispatchers.IO) { playListDaoSource.getPlayListById(id) }
    }

    override suspend fun getAll(): List<PlayList> {
        return withContext(Dispatchers.IO) { playListDaoSource.getAll() }
    }
}