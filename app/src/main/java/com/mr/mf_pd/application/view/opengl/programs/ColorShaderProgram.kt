package com.mr.mf_pd.application.view.opengl.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

class ColorShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.simple_vertex_shader,
    R.raw.simple_fragment_shader
) {
    private val uMatrixLocation: Int = GLES30.glGetUniformLocation(program, U_MATRIX)
    val aPositionLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
    private val uColorLocation: Int = GLES30.glGetUniformLocation(program, U_COLOR)

    fun setUniforms(matrix: FloatArray?, r: Float, g: Float, b: Float) {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES30.glUniform4f(uColorLocation, r, g, b, 1f)
    }

    fun setMatrixL(matrix: FloatArray?,){
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }

    fun setColor(r: Float, g: Float, b: Float){
        GLES30.glUniform4f(uColorLocation, r, g, b, 1f)
    }
}