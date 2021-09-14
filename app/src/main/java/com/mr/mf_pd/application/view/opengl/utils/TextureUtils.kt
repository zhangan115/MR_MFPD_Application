package com.mr.mf_pd.application.view.opengl.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log

object TextureUtils {

    private const val TAG = "TextureUtils"

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
    fun loadTextureWithText(text:String): Int{
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
//        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
//        bitmap.recycle()
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        return textureObjectIds[0]
    }

}