package mor.aliakbar.mymusic.data.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PlayLists")
data class PlayList(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var playListName: String? = null
)