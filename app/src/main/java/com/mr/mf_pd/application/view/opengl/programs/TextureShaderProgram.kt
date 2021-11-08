package com.mr.mf_pd.application.view.opengl.programs

import android.content.Context
import android.opengl.GLES30
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


}