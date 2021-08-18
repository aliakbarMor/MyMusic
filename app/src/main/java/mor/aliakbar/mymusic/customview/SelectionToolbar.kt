package mor.aliakbar.mymusic.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import mor.aliakbar.mymusic.databinding.ViewToolbarSelectionBinding

class SelectionToolbar(context: Context, attrs: AttributeSet?) : ConstraintLayout(context,attrs) {

    private var binding: ViewToolbarSelectionBinding =
        ViewToolbarSelectionBinding.inflate(LayoutInflater.from(context), this, true)

    var onDisableClickListener: OnClickListener? = null
    set(value) {
        field = value
        binding.imageDisable.setOnClickListener(
            onDisableClickListener
        )
    }
    var onActionPlayNextClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.actionPlayNext.setOnClickListener(
                onActionPlayNextClickListener
            )
        }
    var onActionAddToClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.actionAddTo.setOnClickListener(
                onActionAddToClickListener
            )
        }
    var textNumberOfSelected: String? = null
        set(value) {
            field = value
            binding.numberOfSelected.text = value
        }

}