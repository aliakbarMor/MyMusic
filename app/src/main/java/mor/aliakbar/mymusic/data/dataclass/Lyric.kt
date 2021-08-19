package mor.aliakbar.mymusic.data.dataclass

import com.google.gson.annotations.SerializedName

data class Lyric(
    @SerializedName("lyrics")
    var lyrics: String? = null
)