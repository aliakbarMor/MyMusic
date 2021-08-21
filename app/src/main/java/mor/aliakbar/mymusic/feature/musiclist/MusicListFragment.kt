package mor.aliakbar.mymusic.feature.musiclist

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.data.dataclass.PlayList
import mor.aliakbar.mymusic.databinding.DialogAddNewPlaylistBinding
import mor.aliakbar.mymusic.databinding.DialogSleepTimerBinding
import mor.aliakbar.mymusic.databinding.FragmentMusicListBinding
import mor.aliakbar.mymusic.databinding.NavHeaderBinding
import mor.aliakbar.mymusic.services.loadingimage.LoadingImageServices
import mor.aliakbar.mymusic.services.musicservice.MusicService
import mor.aliakbar.mymusic.utility.MusicListenerUtility
import mor.aliakbar.mymusic.utility.Variable
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicListFragment : BaseFragment<FragmentMusicListBinding>(), MusicListener,
    NavigationView.OnNavigationItemSelectedListener {

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
        if (!verticalMusicAdapter.selectedMode) {
            MusicListenerUtility.setMusicListState(isMostPlayedList)
            goToFragmentPlayMusic(position)
        } else toggleToolbar()
    }

    override fun onMusicLongClicked(position: Int) {
        toggleToolbar()
    }

    override fun onSubjectClicked(position: Int, isMostPlayedList: Boolean, view: View) {
        val popup = PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.subject_menu, menu)
        }
        addPlayListsToMenuExceptFavorite(popup.menu.findItem(R.id.addTo).subMenu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.play -> {
                    MusicListenerUtility.setMusicListState(isMostPlayedList)
                    goToFragmentPlayMusic(position)
                }
                R.id.playNext -> {
                    viewModel.playingNextSong(position)
                    verticalMusicAdapter.notifyDataSetChanged()
                }
                R.id.share -> {
                    MusicListenerUtility.shareMusic(
                        requireActivity(), viewModel.currentList.value!![position].path!!
                    )
                }
                R.id.delete -> {
                    MusicListenerUtility.deleteMusic(
                        requireActivity(), viewModel.currentList.value!![position].path!!
                    )
                    Toast.makeText(
                        activity,
                        viewModel.currentList.value!![position].title.toString() + " is deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    if (item.itemId != R.id.addTo) {
                        viewModel.addMusicToPlayList(viewModel.currentList.value!![position].apply {
                            playListName = item.title.toString()
                        })
                    }
                }
            }
            false
        }
        popup.show()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.sleepTimer -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val dialogSleepTimerBinding =
                    DialogSleepTimerBinding.inflate(LayoutInflater.from(requireContext()))
                showDialog(dialogSleepTimerBinding, {
                    val sleepTime = dialogSleepTimerBinding.textSleepTime.text
                    val time = sleepTime.toString().toInt() * 1000 * 60
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            requireActivity().startService(
                                Intent(requireActivity(), MusicService::class.java).apply {
                                    action = MusicService.ACTION_STOP_AND_RESUME
                                })
                        }
                    }, time.toLong())
                })
            }
            R.id.about -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action = MusicListFragmentDirections.actionMusicListToAboutFragment()
                controller.navigate(action)
            }
            R.id.addNewPlaylist -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val dialogBinding =
                    DialogAddNewPlaylistBinding.inflate(LayoutInflater.from(requireContext()))
                showDialog(
                    dialogBinding, {
                        viewModel.insertPlaylist(
                            PlayList(playListName = dialogBinding.tvNamePlaylist.text.toString())
                        )
                    })
            }
            else -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action =
                    MusicListFragmentDirections.actionMusicListSelf(menuItem.title.toString())
                controller.navigate(action)
            }
        }
        return false
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
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(viewModel.musicReceiver, musicIntentFilter)

        viewModel.getMostPlayedMusics()
    }

    private fun initialize() {
        setOnBackClick()
        controller = requireActivity().findNavController(R.id.nav_host_fragment)
        binding.navView.addHeaderView(navBinding.root)
        binding.navView.setNavigationItemSelectedListener(this)

        addPlayListsToMenuExceptFavorite(binding.navView.menu)

        binding.imageMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.bottomSheet.setOnClickListener {
//            viewModel.updateListSateContainer(ListStateType.DEFAULT)
            viewModel.getCurrentSongIndex()?.let { position -> goToFragmentPlayMusic(position) }
        }

        binding.slectionToolbar.onDisableClickListener = View.OnClickListener {
            disableSelectedMode()
        }

        binding.slectionToolbar.onActionAddToClickListener = View.OnClickListener {
            val popup = PopupMenu(activity?.applicationContext, it)
            popup.menu.add(0, 51, Menu.NONE, "Favorite")
            addPlayListsToMenuExceptFavorite(popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                viewModel.insertSomeMusics(viewModel.currentList.value!!
                    .filter { music -> music.isSelected }
                    .onEach { music -> music.playListName = menuItem.title.toString() })
                disableSelectedMode()
                false
            }
            popup.show()
        }

        binding.slectionToolbar.onActionPlayNextClickListener = View.OnClickListener {
            viewModel.currentList.value!!
                .filter { music -> music.isSelected }
                .forEach { viewModel.playingNextSong(viewModel.currentList.value!!.indexOf(it)) }
            disableSelectedMode()
        }

        if (!viewModel.isInPlayList.value!!) {
            binding.textSearch.addTextChangedListener {
                viewModel.search(binding.textSearch.text.toString())
            }

            binding.imgSearch.setOnClickListener {
                viewModel.search(binding.textSearch.text.toString())
            }
        }

    }

    private fun toggleToolbar() {
        verticalMusicAdapter.selectedMode = verticalMusicAdapter.musicPositionSelected.size != 0
        if (verticalMusicAdapter.selectedMode) {
            binding.mainToolbar.visibility = View.GONE
            binding.slectionToolbar.visibility = View.VISIBLE
        } else {
            binding.slectionToolbar.visibility = View.GONE
            if (!viewModel.isInPlayList.value!!)
                binding.mainToolbar.visibility = View.VISIBLE
        }

        binding.slectionToolbar.textNumberOfSelected =
            verticalMusicAdapter.musicPositionSelected.size.toString() + " selected"
    }

    private fun disableSelectedMode() {
        viewModel.currentList.value!!.forEach {
            it.isSelected = false
        }
        verticalMusicAdapter.musicPositionSelected.clear()
        verticalMusicAdapter.notifyDataSetChanged()
        toggleToolbar()
    }

    private fun loadRandomImage() {
        if (viewModel.currentList.value?.isNotEmpty() == true) {
            val random = Random()
            CoroutineScope(Dispatchers.IO).launch {
                delay(50)
                while (this@MusicListFragment.isResumed) {
                    binding.imageMusic.post {
                        glideLoadingImageServices.loadBigImage(
                            binding.imageMusic,
                            viewModel.currentList.value!![random.nextInt(viewModel.currentList.value!!.size)].path
                        )
                    }
                    delay(6000)
                }
            }
        }
    }

    private fun observeViews() {
        viewModel.musicListHorizontal.observe(viewLifecycleOwner, {
            horizontalMusicAdapter.viewType = Variable.MUSIC_VIEW_TYPE_HORIZONTAL
            horizontalMusicAdapter.musics = it as ArrayList<Music>
            horizontalMusicAdapter.musicListener = this
            binding.recyclerViewHorizontal.adapter = horizontalMusicAdapter
        })

        viewModel.currentList.observe(viewLifecycleOwner, {
            verticalMusicAdapter.musics = it as ArrayList<Music>
            verticalMusicAdapter.musicListener = this
            binding.recyclerView.adapter = verticalMusicAdapter
        })

        viewModel.isInPlayList.observe(viewLifecycleOwner) {
            if (it) {
                binding.recyclerViewHorizontal.visibility = View.GONE
                binding.mainToolbar.visibility = View.GONE
                binding.imageMusic.visibility = View.VISIBLE
                binding.appBarLayout.setBackgroundColor(Color.WHITE)
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                loadRandomImage()
            }
        }

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

        viewModel.percentageTime.observe(viewLifecycleOwner) {
            binding.seekBar.progress = it
        }
    }

    private fun addPlayListsToMenuExceptFavorite(menu: Menu) {
        viewModel.playLists.observe(viewLifecycleOwner) {
            menu.removeItem(52)
            it.forEach { playList ->
                if (playList.playListName != "Favorite") {
                    menu.add(0, 52, Menu.NONE, playList.playListName)
                        .setIcon(R.drawable.ic_music)
                }
            }
        }
    }

    private fun setOnBackClick() {
        activity?.onBackPressedDispatcher?.addCallback {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (verticalMusicAdapter.selectedMode) {
                disableSelectedMode()
            } else if (!controller.popBackStack()) {
                requireActivity().finish()
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


}