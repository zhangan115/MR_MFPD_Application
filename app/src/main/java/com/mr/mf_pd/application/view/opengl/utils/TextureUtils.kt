package com.mr.mf_pd.application.view.opengl.utils

import android.content.Context
import android.graphics.*
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.TextBitmap
import com.sito.tool.library.utils.DisplayUtil
import java.util.ArrayList

object TextureUtils {

    private const val TAG = "TextureUtils"

    var height: Int = 0
    var width: Int = 0

    @JvmStatic
    fun loadTexture(context: Context, resourced: Int): Int {
        val textureObjectIds = IntArray(1)
        GLES30.glGenTextures(1, textureObjectIds, 0)
        if (textureObjectIds[0] == 0) {
            Log.d(TAG, "could not generate a openGl texture object.")
            return 0
        }
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourced, options)
        if (bitmap == null) {
            Log.d(TAG, "resource Id" + resourced + "could not be decoded")
            GLES30.glDeleteTextures(1, textureObjectIds, 0)
            return 0
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0])
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }

    @JvmStatic
    fun loadTextureWithText(context: Context, texts: Map<String, ArrayList<String>>): Int {
        val textureObjectIds = IntArray(1)
        GLES30.glGenTextures(1, textureObjectIds, 0)
        if (textureObjectIds[0] == 0) {
            Log.d(TAG, "could not generate a openGl texture object.")
            return 0
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0])
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        val bitmap = createBitmap(context, texts).bitmap
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }

    private fun createBitmap(
        context: Context,
        textMaps: Map<String, ArrayList<String>>
    ): TextBitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        //背景颜色
        canvas.drawColor(Color.WHITE)
        val p = Paint()
        //字体设置
        val fontType = "宋体"
        val typeface = Typeface.create(fontType, Typeface.NORMAL)
        //消除锯齿
        p.isAntiAlias = true
        //字体为红色
        p.color = context.getColor(R.color.text_title)
        p.typeface = typeface
        p.textSize = DisplayUtil.sp2px(context,12f).toFloat()
        DisplayUtil.px2dip(context, height.toFloat())
        //绘制字体
        val step = (width - 0.3 * width  )/ 2 / 4
        textMaps.entries.forEach {
            when (it.key) {
                Constants.KEY_UNIT -> {
                    canvas.drawText(it.value[0], 10f, 50f, p)
                }
                Constants.KEY_X_TEXT -> {
                    for (i in 0 until it.value.size) {
                        canvas.drawText(it.value[i], (i * step + 81).toFloat(), height-30f, p)
                    }
                }
                Constants.KEY_Y_TEXT -> {

                }
            }
        }
        return TextBitmap(bitmap)
    }

}