package mor.aliakbar.mymusic.utility

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics

object Utils {

    fun milliToMinutes(millisecondString: String): String {
        if (millisecondString.toLong() > 0) {
            val millisecond = millisecondString.toLong()
            var hours = (millisecond / (60 * 60 * 1000)).toString()
            var minutes = (millisecond % (60 * 60 * 1000) / (60 * 1000)).toString()
            var seconds = (millisecond % (60 * 60 * 1000) % (60 * 1000) / 1000).toString()


            if (Integer.parseInt(hours) < 10)
                hours = "0$hours"
            if (Integer.parseInt(minutes) < 10)
                minutes = "0$minutes"
            if (Integer.parseInt(seconds) < 10)
                seconds = "0$seconds"

            return if (Integer.parseInt(hours) > 0)
                "$hours:$minutes:$seconds"
            else
                "$minutes:$seconds"
        }
        return "00:00"
    }

    fun convertDpToPixel(dp: Float, context: Context?): Float {
        return if (context != null) {
            val resources = context.resources
            val metrics = resources.displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            val metrics = Resources.getSystem().displayMetrics
            dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

    fun removeNameSiteFromMusic(name: String): String {
        val start = name.indexOf('[')

        return if (start != -1){
            name.substring(0, start)
        }else name
    }


}
