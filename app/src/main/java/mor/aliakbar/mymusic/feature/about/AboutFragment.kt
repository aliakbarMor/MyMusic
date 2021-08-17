package mor.aliakbar.mymusic.feature.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import mor.aliakbar.mymusic.base.BaseFragment
import mor.aliakbar.mymusic.databinding.FragmentAboutBinding

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAboutBinding
        get() = { layoutInflater, viewGroup, b ->
            FragmentAboutBinding.inflate(layoutInflater, viewGroup, b)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }

}