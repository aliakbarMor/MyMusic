package mor.aliakbar.mymusic.services.loadingimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mor.aliakbar.mymusic.R
import mor.aliakbar.mymusic.utility.Utils.convertDpToPixel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlideLoadingImage @Inject constructor(@ApplicationContext private var context: Context) :
    LoadingImageServices {

    private var littleCircleImageSize = convertDpToPixel(50f, context).toInt()
    private var mediumImageWidth = convertDpToPixel(130f, context).toInt()
    private var mediumImageHeight = convertDpToPixel(160f, context).toInt()

    override fun loadLittleCircleImage(imageView: ImageView, path: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmapRequestBuilder = loadImage(imageView, path)
                ?.override(littleCircleImageSize, littleCircleImageSize)
                ?.circleCrop()
            imageView.post { bitmapRequestBuilder?.into(imageView) }
        }
    }

    override fun loadMediumImage(imageView: ImageView, path: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val newBitmapRequestBuilder = loadImage(imageView, path)
                ?.override(mediumImageWidth, mediumImageHeight)
            imageView.post { newBitmapRequestBuilder?.into(imageView) }
        }
    }

    override fun loadBigImage(imageView: ImageView, path: String?) {
        loadImage(imageView, path)
            ?.into(imageView)
    }

    override fun loadCenterCropImage(imageView: ImageView, path: String?) {
        imageView.post {
            loadImage(imageView, path)
                ?.override(imageView.measuredWidth, 1000)
                ?.centerCrop()
                ?.into(imageView)
        }
    }


    private fun loadImage(imageView: ImageView, path: String?): RequestBuilder<Bitmap>? {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(path)
        val art = metaRetriever.embeddedPicture
        if (art != null) {
            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
            return Glide
                .with(imageView.context)
                .asBitmap()
                .load(songImage)
        } else
            imageView.post { imageView.setImageResource(R.drawable.ic_music) }
        return null
    }

}
