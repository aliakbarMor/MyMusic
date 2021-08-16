package mor.aliakbar.mymusic.services.loadingimage

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.bumptech.glide.Glide
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
        val metaRetriever = MediaMetadataRetriever()
        CoroutineScope(Dispatchers.IO).launch {
            metaRetriever.setDataSource(path)
            val art = metaRetriever.embeddedPicture
            if (art != null) {
                val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                val bitmapRequestBuilder = Glide
                    .with(imageView.context)
                    .asBitmap()
                    .override(littleCircleImageSize, littleCircleImageSize)
                    .load(songImage)
                    .circleCrop()
                imageView.post { bitmapRequestBuilder.into(imageView) }
            } else {
                imageView.post { imageView.setImageResource(R.drawable.ic_music) }
            }
        }
    }

    override fun loadMediumImage(imageView: ImageView, path: String?) {
        val metaRetriever = MediaMetadataRetriever()
        CoroutineScope(Dispatchers.IO).launch {
            metaRetriever.setDataSource(path)
            val art = metaRetriever.embeddedPicture
            if (art != null) {
                val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                val bitmapRequestBuilder = Glide
                    .with(imageView.context)
                    .asBitmap()
                    .override(mediumImageWidth, mediumImageHeight)
                    .load(songImage)
                imageView.post { bitmapRequestBuilder.into(imageView) }
            } else {
                imageView.post { imageView.setImageResource(R.drawable.ic_music) }
            }
        }
    }

    override fun loadBigImage(imageView: ImageView, path: String?) {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(path)
        val art = metaRetriever.embeddedPicture
        if (art != null) {
            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
            Glide
                .with(imageView.context)
                .asBitmap()
                .load(songImage)
                .into(imageView)
        } else
            imageView.setImageResource(R.drawable.ic_music)
    }
}
