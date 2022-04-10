package com.mr.mf_pd.application.opengl.utils

import android.content.Context
import android.graphics.*
import android.opengl.GLES30
import android.opengl.GLUtils
import android.text.TextUtils
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.opengl.TextBitmap
import com.mr.mf_pd.application.opengl.`object`.TextRectInOpenGl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object TextureUtils {

    private const val TAG = "TextureUtils"

    var height: Int = 0
    var width: Int = 0

    @JvmStatic
    fun loadTextureWithText(
        p: Paint,
        textRect: TextRectInOpenGl,
        texts: ConcurrentHashMap<String, CopyOnWriteArrayList<String>>,
        textureId: Int,
    ): Int {
        val oldTextureObjectIds = IntArray(1)
        oldTextureObjectIds[0] = textureId
        GLES30.glDeleteTextures(1, oldTextureObjectIds, 0)
        val textureObjectIds = IntArray(1)
        GLES30.glGenTextures(1, textureObjectIds, 0)
        if (textureObjectIds[0] == 0) {
            Log.d(TAG, "could not generate a openGl texture object.")
            return 0
        }
        GLES30.glDeleteTextures(1, textureObjectIds, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectIds[0])
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        val bitmap = createBitmap(p, textRect, texts).bitmap
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureObjectIds[0]
    }

    /**
     * @param p 画笔
     * @param textRect 最大字数的范围
     * @param textMaps 需要绘出的字符数据
     */
    private fun createBitmap(
        p: Paint,
        textRect: TextRectInOpenGl,
        textMaps: ConcurrentHashMap<String, CopyOnWriteArrayList<String>>,
    ): TextBitmap {
        val bitmap = Bitmap.createBitmap(width,
            height,
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        //背景颜色
        canvas.drawColor(Color.TRANSPARENT)
        val rect = Rect()
        if (textMaps.containsKey(Constants.KEY_Z_TEXT)) {
            val value = textMaps[Constants.KEY_Z_TEXT]
            if (value != null) {
                for (i in 0 until value.size) {
                    val startY = 2 * textRect.textHeightGraphics
                    val step = (textRect.heightGraphics - 2 * startY) / (value.size - 1)
                    val startX = textRect.textWidthGraphics * 0.25f
                    p.getTextBounds(value[i], 0, value[i].length, rect)
                    canvas.drawText(value[i],
                        startX,
                        (step * i + startY),
                        p)
                }
            }
            val unit = textMaps[Constants.KEY_UNIT]?.firstOrNull()
            if (!TextUtils.isEmpty(unit)) {
                p.getTextBounds(unit, 0, unit!!.length, rect)
                canvas.drawText(unit, 2 * textRect.textWidthGraphics, rect.height().toFloat(), p)
            }
        } else {
            canvas.drawColor(Color.TRANSPARENT)
            textMaps.entries.forEach {
                when (it.key) {
                    Constants.KEY_UNIT -> {
                        if (it.value.isNotEmpty()) {
                            val x = textRect.textWidthGraphics * 1.5f
                            it.value.firstOrNull()?.let { it1 ->
                                canvas.drawText(it1, x,
                                    textRect.textHeightGraphics, p)
                            }
                        }
                    }
                    Constants.KEY_X_TEXT -> {
                        val startX = textRect.textWidthGraphics * 1.5f
                        val step =
                            (textRect.widthGraphics - startX - textRect.textWidthGraphics) / (it.value.size - 1)
                        val y = textRect.heightGraphics - textRect.textHeightGraphics / 2
                        for (i in 0 until it.value.size) {
                            p.getTextBounds(it.value[i], 0, it.value[i].length, rect)
                            canvas.drawText(
                                it.value[i],
                                (i * step + startX) - (rect.width() / 2f),
                                y,
                                p
                            )
                        }
                    }
                    Constants.KEY_Y_TEXT -> {
                        val startY = textRect.textHeightGraphics * 2f
                        val step = (textRect.heightGraphics - 2 * startY) / (it.value.size - 1)
                        val startX = textRect.textWidthGraphics * 0.25f
                        for (i in 0 until it.value.size) {
                            canvas.drawText(it.value[i],
                                startX,
                                (step * i + startY) + textRect.rect.height() / 2,
                                p)
                        }
                    }
                }
            }
        }
        return TextBitmap(bitmap)
    }

}