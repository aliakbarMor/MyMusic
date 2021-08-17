package mor.aliakbar.mymusic.feature.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.databinding.FragmentPlayListBinding
import mor.aliakbar.mymusic.feature.musiclist.MusicAdapter
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PlayListFragment : BaseFragment<FragmentPlayListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlayListBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentPlayListBinding.inflate(layoutInflater, viewGroup, b)
        }
    private val viewModel: PlayListViewModel by viewModels()

    @Inject lateinit var musicAdapter: MusicAdapter
    @Inject lateinit var glideLoadingImageServices: LoadingImageServices

    private val timer = Timer()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViews()

    }

    private fun observeViews() {
        viewModel.musicList.observe(viewLifecycleOwner) {
            musicAdapter.musics = it as ArrayList<Music>
            binding.recyclerView.adapter = musicAdapter
//            musicAdapter!!.musicListener = this

            if (it.isNotEmpty())
                loadRandomImage()
        }
    }

    private fun loadRandomImage() {
        if (viewModel.musicList.value!!.isNotEmpty()) {
            val random = Random()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    binding.imageMusic.post {
                        glideLoadingImageServices.loadBigImage(
                            binding.imageMusic,
                            viewModel.musicList.value!![random.nextInt(viewModel.musicList.value!!.size)].path
                        )
                    }
                }
            }, 0, 6000)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

}