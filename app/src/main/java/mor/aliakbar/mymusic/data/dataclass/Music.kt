package mor.aliakbar.mymusic.data.dataclass

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "Musics")
class Music(
    var musicId: String?,
    var artist: String?,
    var title: String?,
    var path: String?,
    var duration: String?,
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var playListName: String? = null
    var numberOfPlayedSong: Long = 0

    @Ignore
    var isSelected = false

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readLong()
        playListName = parcel.readString()
        numberOfPlayedSong = parcel.readLong()
        isSelected = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(musicId)
        parcel.writeString(artist)
        parcel.writeString(title)
        parcel.writeString(path)
        parcel.writeString(duration)
        parcel.writeLong(id)
        parcel.writeString(playListName)
        parcel.writeLong(numberOfPlayedSong)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }

}