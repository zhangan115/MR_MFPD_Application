package com.mr.mf_pd.application.view.opengl.programs

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.glGenerateMipmap
import android.opengl.GLES30
import android.opengl.GLUtils
import com.mr.mf_pd.application.R


class TextureShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.texture_vertex_shader,
    R.raw.texture_fragment_shader
) {

    private val uMatrixLocation: Int = GLES30.glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation: Int = GLES30.glGetUniformLocation(program, U_TEXTURE_UNIT)

    val positionAttributeLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
    val textureCoordinatesAttributeLocation: Int =
        GLES30.glGetAttribLocation(program, A_TEXTURE_COORDINATES)


    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)
    }

    fun setUniforms(textureId: Int) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)
    }

    fun storeImage(bitmap: Bitmap) {
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        GLES30.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        bitmap.recycle()
    }

}