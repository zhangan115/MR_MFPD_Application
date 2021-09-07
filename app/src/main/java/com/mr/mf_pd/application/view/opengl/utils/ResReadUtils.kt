package com.mr.mf_pd.application.view.opengl.utils

import android.content.res.Resources.NotFoundException
import com.mr.mf_pd.application.app.MRApplication
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

public object ResReadUtils {
    /**
     * 读取资源
     *
     * @param resourceId
     * @return
     */
    @JvmStatic
   open fun readResource(resourceId: Int): String {
        val builder = StringBuilder()
        try {
            val inputStream = MRApplication.instance.resources.openRawResource(resourceId)
            val streamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(streamReader)
            var textLine: String?
            while (bufferedReader.readLine().also { textLine = it } != null) {
                builder.append(textLine)
                builder.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        return builder.toString()
    }

}