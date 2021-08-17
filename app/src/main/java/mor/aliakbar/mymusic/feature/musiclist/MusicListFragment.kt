package mor.aliakbar.mymusic.feature.musiclist

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.databinding.FragmentMusicListBinding
import mor.aliakbar.mymusic.databinding.NavHeaderBinding
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import mor.aliakbar.mymusic.services.musicservice.MusicService
import mor.aliakbar.mymusic.utility.Variable
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicListFragment : BaseFragment<FragmentMusicListBinding>(), MusicListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMusicListBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentMusicListBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var navBinding: NavHeaderBinding
    private val viewModel: MusicListViewModel by viewModels()

    private lateinit var controller: NavController

    @Inject lateinit var verticalMusicAdapter: MusicAdapter
    @Inject lateinit var horizontalMusicAdapter: MusicAdapter
    @Inject lateinit var glideLoadingImageServices: LoadingImageServices

    override fun onMusicClicked(position: Int, isMostPlayedList: Boolean) {
        if (isMostPlayedList)
            viewModel.updateListSateContainer(ListStateType.MOST_PLAYED)
        else if (ListStateContainer.state != ListStateType.FILTERED && ListStateContainer.state != ListStateType.CUSTOM)
            viewModel.updateListSateContainer(ListStateType.DEFAULT)
        goToFragmentPlayMusic(position)
    }

    override fun onMusicLongClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onSubjectClicked(position: Int, isMostPlayedList: Boolean, view: View) {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        navBinding = NavHeaderBinding.inflate(inflater)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()
        initialize()
        observeViews()

    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_STARTED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(musicReceiver, musicIntentFilter)
    }

    private fun initialize() {
        controller = requireActivity().findNavController(R.id.nav_host_fragment)
        binding.navView.addHeaderView(navBinding.root)

        binding.bottomSheet.setOnClickListener {
            viewModel.updateListSateContainer(ListStateType.DEFAULT)
            viewModel.getCurrentSongIndex()?.let { position -> goToFragmentPlayMusic(position) }
        }
    }

    private fun observeViews() {
        viewModel.musicListHorizontal.observe(viewLifecycleOwner, {
            horizontalMusicAdapter.viewType = Variable.MUSIC_VIEW_TYPE_HORIZONTAL
            horizontalMusicAdapter.musics = it as ArrayList<Music>
            horizontalMusicAdapter.musicListener = this
            binding.recyclerViewHorizontal.adapter = horizontalMusicAdapter
        })

        viewModel.musicList.observe(viewLifecycleOwner, {
            verticalMusicAdapter.musics = it as ArrayList<Music>
            verticalMusicAdapter.musicListener = this
            binding.recyclerView.adapter = verticalMusicAdapter
        })

        viewModel.lastMusicPlayed.observe(viewLifecycleOwner) {
            if (it.path != null) {
                val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.apply {
                    textTitleBottomSheet.text = it.title
                    textArtistBottomSheet.text = it.artist
                    glideLoadingImageServices.loadMediumImage(imageArtistBottomSheet, it.path)
                }
                navBinding.apply {
                    textTitleNavigation.text = it.title
                    textArtistNavigation.text = it.artist
                    glideLoadingImageServices.loadCenterCropImage(imageViewNavigation, it.path)
                }
            }
        }

    }

    private fun checkPermission() {
        val havePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!havePermission) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Variable.MY_PERMISSIONS_MUSIC
                )
            }
        }
        if (!havePermission) {
            Thread.sleep(100)
            checkPermission()
        }
    }

    private fun goToFragmentPlayMusic(position: Int) {
        val action = MusicListFragmentDirections.actionMusicListToPlayMusic(position)
        controller.navigate(action)
    }

    private fun setNavViewAndBottomShit(music: Music) {

    }


    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            val bundle = intent.extras
            when (action) {
                MusicService.ACTION_MUSIC_STARTED -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val music =
                            viewModel.getCurrentMusicList()[bundle!!.getInt("currentPosition")]
                        viewModel.saveLastMusicPlayed(music)
                    }
                }
                MusicService.ACTION_MUSIC_IN_PROGRESS -> {
                    binding.seekBar.progress =
                        bundle!!.getInt("currentPositionTime") * 100 / viewModel.lastMusicPlayed.value?.duration!!.toInt()
                }
            }
        }
    }


}