package mor.aliakbar.mymusic.feature.playmusic

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.databinding.DialogLyricBinding
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

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_STOP_AND_RESUME)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(viewModel.musicReceiver, musicIntentFilter)
    }

    private fun observesView() {
        viewModel.music.observe(viewLifecycleOwner) {
            binding.apply {
                textTitle.text = it.title
                textArtist.text = it.artist
                totalDuration.text = Utils.milliToMinutes(it.duration!!)
                glideLoadingImage.loadBigImage(imageMusic, it.path)
                textWitchSong.text =
                    "${viewModel.position + 1}/${viewModel.musicsList.value!!.size}"
                btnPlay.setImageResource(R.mipmap.ic_pause)
            }
            if (viewModel.checkIsNewSong() || !MusicService.isServiceStart)
                requireActivity().startService(
                    Intent(requireActivity(), MusicService::class.java).apply {
                        action = MusicService.ACTION_PLAY
                        putExtra("position", viewModel.position)
                    })
            else if (!viewModel.mediaPlayer.isPlaying)
                binding.btnPlay.setImageResource(R.mipmap.ic_play)

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
                binding.btnPlay.setImageResource(R.mipmap.ic_pause)
            } else {
                binding.btnPlay.setImageResource(R.mipmap.ic_play)
            }
        }

        viewModel.currentPositionTime.observe(viewLifecycleOwner) {
            binding.currentDuration.text = Utils.milliToMinutes(it.toString())
            if (it >= 0) {
                if (viewModel.music.value != null)
                    binding.seekBar.progress =
                        it.toInt() * 100 / viewModel.music.value?.duration!!.toInt()
            }
        }

        viewModel.isRepeat.observe(viewLifecycleOwner) {
            if (it)
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            else
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) {
            if (it) {
                binding.btnFavourite.setImageResource(R.drawable.ic_favorite)
            } else {
                binding.btnFavourite.setImageResource(R.drawable.ic_not_favorite)
            }
        }
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
            requireActivity().startService(
                Intent(requireActivity(), MusicService::class.java).apply {
                    action = MusicService.ACTION_STOP_AND_RESUME
                })
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
            val dialogBinding = DialogLyricBinding.inflate(LayoutInflater.from(requireContext()))
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getLyric()
                    .catch {
                        delay(500)
                        dialogBinding.lyricBody.text = "No embedded lyric found"
                    }
                    .collect {
                        dialogBinding.lyricBody.text = it.lyrics
                    }
            }
            showDialog(dialogBinding)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}
