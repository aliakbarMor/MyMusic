package mor.aliakbar.mymusic.feature.playmusic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.databinding.FragmentPlayMusicBinding
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import mor.aliakbar.mymusic.utility.Utils
import javax.inject.Inject

@AndroidEntryPoint
class PlayMusicFragment : BaseFragment<FragmentPlayMusicBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlayMusicBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentPlayMusicBinding.inflate(layoutInflater, viewGroup, b)
        }
    private val viewModel: PlayMusicViewModel by viewModels()

    @Inject
    lateinit var glideLoadingImage: LoadingImageServices

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.music.observe(viewLifecycleOwner) {
            binding.apply {
                textTitle.text = it.title
                textArtist.text = it.artist
                totalDuration.text = Utils.milliToMinutes(it.duration!!)
                glideLoadingImage.loadBigImage(imageMusic, it.path)
                textWitchSong.text = "${viewModel.position}/${viewModel.musicsList.value!!.size}"
            }
        }
    }
}
