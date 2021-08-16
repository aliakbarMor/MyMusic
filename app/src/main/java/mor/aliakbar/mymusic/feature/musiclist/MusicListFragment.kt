package mor.aliakbar.mymusic.feature.musiclist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.data.dataclass.ListStateContainer
import mor.aliakbar.mymusic.data.dataclass.ListStateType
import mor.aliakbar.mymusic.data.dataclass.Music
import mor.aliakbar.mymusic.databinding.FragmentMusicListBinding
import mor.aliakbar.mymusic.utility.Variable
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicListFragment : BaseFragment<FragmentMusicListBinding>(), MusicListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMusicListBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentMusicListBinding.inflate(layoutInflater, viewGroup, b)
        }
    private val viewModel: MusicListViewModel by viewModels()

    private lateinit var controller: NavController

    @Inject
    lateinit var verticalMusicAdapter: MusicAdapter

    @Inject
    lateinit var horizontalMusicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = requireActivity().findNavController(R.id.nav_host_fragment)

        checkPermission()
        observeViews()

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
            if (it.artist != null) {
//                setNavViewAndBottomShit(it)
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

    private fun goToFragmentPlayMusic(position: Int) {
        val action = MusicListFragmentDirections.actionMusicListToPlayMusic(position)
        controller.navigate(action)
    }


}