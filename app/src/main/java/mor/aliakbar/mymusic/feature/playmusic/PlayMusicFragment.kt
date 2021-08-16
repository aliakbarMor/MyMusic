package mor.aliakbar.mymusic.feature.playmusic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.databinding.FragmentPlayMusicBinding
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import mor.aliakbar.mymusic.services.musicservice.MusicService
import mor.aliakbar.mymusic.utility.Utils
import javax.inject.Inject

@AndroidEntryPoint
class PlayMusicFragment : BaseFragment<FragmentPlayMusicBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlayMusicBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentPlayMusicBinding.inflate(layoutInflater, viewGroup, b)
        }
    private val viewModel: PlayMusicViewModel by viewModels()
    @Inject lateinit var glideLoadingImage: LoadingImageServices

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observesView()
        setListeners()

    }

    private fun observesView() {
        viewModel.music.observe(viewLifecycleOwner) {
            binding.apply {
                textTitle.text = it.title
                textArtist.text = it.artist
                totalDuration.text = Utils.milliToMinutes(it.duration!!)
                glideLoadingImage.loadBigImage(imageMusic, it.path)
                textWitchSong.text = "${viewModel.position}/${viewModel.musicsList.value!!.size}"
            }

            requireActivity().startService(
                Intent(requireActivity(), MusicService::class.java).apply {
                    action = MusicService.ACTION_PLAY
                    putExtra("position", viewModel.position)
                })
        }

        viewModel.toastMassage.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.stateMusic.observe(viewLifecycleOwner) {
            requireActivity().startService(
                Intent(requireActivity(), MusicService::class.java).apply {
                    action = MusicService.ACTION_CHANGE_STATE
                    putExtra("change state", it)
                })
        }

        viewModel.isPlay.observe(viewLifecycleOwner) {
            if (it) {
                requireActivity().startService(
                    Intent(requireActivity(), MusicService::class.java).apply {
                        action = MusicService.ACTION_RESUME
                        putExtra("position", viewModel.position)
                        putExtra("currentPositionTime", viewModel.currentPositionTime.value!!)
                    })
            } else {
                requireActivity().startService(
                    Intent(requireActivity(), MusicService::class.java).apply {
                        action = MusicService.ACTION_STOP
                        putExtra("position", viewModel.position)
                        putExtra("currentPositionTime", viewModel.currentPositionTime.value!!)
                    })
            }
        }

        viewModel.currentPositionTime.observe(viewLifecycleOwner, {
            binding.currentDuration.text = Utils.milliToMinutes(it.toString())
            if (it >= 0) {
                binding.seekBar.progress =
                    it.toInt() * 100 / viewModel.music.value!!.duration!!.toInt()
            }
        })

        viewModel.isRepeat.observe(viewLifecycleOwner, {
            if (it)
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            else
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
        })

        viewModel.isPlay.observe(viewLifecycleOwner, {
            if (it) {
                binding.btnPlay.setImageResource(R.mipmap.ic_pause)
            } else {
                binding.btnPlay.setImageResource(R.mipmap.ic_play)
            }
        })

        viewModel.isFavorite.observe(viewLifecycleOwner, {
            if (it) {
                binding.btnFavourite.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.btnFavourite.setImageResource(R.drawable.ic_not_favorite)
            }
        })
    }


    private fun setListeners() {
        binding.btnSkipNext.setOnClickListener {
            viewModel.skipNext()
        }
        binding.btnSkipPrevious.setOnClickListener {
            viewModel.skipPrevious()
        }
        binding.btnRepeat.setOnClickListener {
            viewModel.onRepeatClicked()
        }
        binding.btnShuffle.setOnClickListener {
            viewModel.onShuffleClicked()
        }
        binding.btnFavourite.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
        binding.btnPlay.setOnClickListener {
            viewModel.onPauseAndPlayClicked()
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.mediaPlayer.seekTo(
                        seekBar.progress * viewModel.music.value!!.duration!!.toInt() / 100
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        binding.btnLyrics.setOnClickListener {
//            TODO
//            LyricDialog.getInstance().showDialog(
//                requireContext(), viewModel.music.value!!.artist, viewModel.music.value!!.title
//            )
        }
    }
}
