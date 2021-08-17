package mor.aliakbar.mymusic.services.loadingimage

import android.widget.ImageView


interface LoadingImageServices {

    fun loadLittleCircleImage(imageView: ImageView, path: String?)

    fun loadMediumImage(imageView: ImageView, path: String?)

    fun loadBigImage(imageView: ImageView, path: String?)

    fun loadCenterCropImage(imageView: ImageView, path: String?)

}
