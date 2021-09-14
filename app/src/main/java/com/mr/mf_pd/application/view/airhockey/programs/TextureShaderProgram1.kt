
package com.mr.mf_pd.application.view.airhockey.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

open class TextureShaderProgram1(context: Context) : ShaderProgram(
    context, R.raw.texture_vertex_shader_1,
    R.raw.texture_fragment_shader_1
) {

    private val uMatrixLocation: Int = GLES30.glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation: Int = GLES30.glGetUniformLocation(program, U_TEXTURE_UNIT)

    open var positionAttributeLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
   open var textureCoordinatesAttributeLocation: Int =
        GLES30.glGetAttribLocation(program, A_TEXTURE_COORDINATES)


    fun setUniforms(textureId: Int) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureUnitLocation, 0)
    }


}