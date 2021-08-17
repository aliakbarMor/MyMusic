package mor.aliakbar.mymusic.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import mor.aliakbar.mymusic.R

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun showDialog(dialogBinding: ViewBinding, onSaveClick: () -> Unit) {
        AlertDialog
            .Builder(context, R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Save") { _, _ ->
                onSaveClick.invoke()
            }
            .show()
    }

}