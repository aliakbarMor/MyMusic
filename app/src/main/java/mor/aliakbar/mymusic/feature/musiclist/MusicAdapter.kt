package mor.aliakbar.mymusic.feature.musiclist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.databinding.ItemMusicHorizontaBinding
import mor.aliakbar.mymusic.databinding.ItemMusicVerticalBinding
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import mor.aliakbar.mymusic.utility.Utils
import mor.aliakbar.mymusic.utility.Variable
import java.util.*
import javax.inject.Inject

class MusicAdapter @Inject constructor(private val loadingImage: LoadingImageServices) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var musics = ArrayList<Music>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var viewType: Int = Variable.MUSIC_VIEW_TYPE_VERTICAL
    var musicListener: MusicListener? = null
    var selectedMode: Boolean = false

    var musicPositionSelected = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Variable.MUSIC_VIEW_TYPE_VERTICAL) {
            VerticalMusicListViewHolder(
                ItemMusicVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            HorizontalMusicListViewHolder(
                ItemMusicHorizontaBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val music = musics[position]
        if (holder is VerticalMusicListViewHolder) {
            holder.bind(music, position)
        } else if (holder is HorizontalMusicListViewHolder) {
            holder.bind(music, position)
        }
    }

    override fun getItemCount(): Int = musics.size

    override fun getItemViewType(position: Int): Int = viewType

    private fun toggleSelection(music: Music, pos: Int) {
        if (music.isSelected) {
            music.isSelected = false
            musicPositionSelected.remove(pos)
            notifyItemChanged(pos)
        } else {
            music.isSelected = true
            musicPositionSelected.add(pos)
            notifyItemChanged(pos)
        }
    }

    fun updateList(newList: ArrayList<Music>) {
        musics = newList
    }


    inner class VerticalMusicListViewHolder(itemMusicBinding: ItemMusicVerticalBinding) :
        RecyclerView.ViewHolder(itemMusicBinding.root) {
        private val binding = itemMusicBinding

        fun bind(music: Music, position: Int) {
            binding.apply {
                textNameMusic.text = music.title
                textNameSinger.text = music.artist
                textTime.text = Utils.milliToMinutes(music.duration!!)
                loadingImage.loadLittleCircleImage(imageMusic, music.path)

                imageSubject.setOnClickListener {
                    musicListener?.onSubjectClicked(position, false, imageSubject)
                }
                binding.root.setOnClickListener {
                    if (selectedMode)
                        toggleSelection(music, position)
                    musicListener?.onMusicClicked(position, false)
                }
                binding.root.setOnLongClickListener {
                    toggleSelection(music, position)
                    musicListener?.onMusicLongClicked(position)
                    return@setOnLongClickListener true
                }

                if (music.isSelected) {
                    iconSelect.visibility = View.VISIBLE
                } else
                    iconSelect.visibility = View.GONE

            }
        }
    }

    inner class HorizontalMusicListViewHolder(itemMusicBinding: ItemMusicHorizontaBinding) :
        RecyclerView.ViewHolder(itemMusicBinding.root) {
        private val binding = itemMusicBinding
        fun bind(music: Music, position: Int) {
            binding.apply {
                textNameMusic.text = music.title
                textNameSinger.text = music.artist
                loadingImage.loadMediumImage(imgMusic, music.path)
                imageSubject.setOnClickListener {
                    musicListener?.onSubjectClicked(position, false, imageSubject)
                }
                binding.root.setOnClickListener {
                    musicListener?.onMusicClicked(position, true)
                }
            }
        }
    }

}