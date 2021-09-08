/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.mr.mf_pd.application.view.opengl.study.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

class ColorShaderProgram(context: Context?) : ShaderProgram(
    context, R.raw.simple_vertex_shader,
    R.raw.simple_fragment_shader
) {
    val uMatrixLocation: Int
    val aPositionLocation: Int
    val aColorLocation: Int
    val uColorLocation: Int


    fun setUniforms(matrix: FloatArray?) {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

    fun setUniforms(matrix: FloatArray?, r: Float, g: Float, b: Float) {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES30.glUniform4f(uColorLocation, r, g, b, 1f)
    }

    init {
        uMatrixLocation = GLES30.glGetUniformLocation(program, U_MATRIX)
        uColorLocation = GLES30.glGetUniformLocation(program, U_COLOR)

        aPositionLocation = GLES30.glGetAttribLocation(program, A_POSITION)
        aColorLocation = GLES30.glGetAttribLocation(program, A_COLOR)

    }
}