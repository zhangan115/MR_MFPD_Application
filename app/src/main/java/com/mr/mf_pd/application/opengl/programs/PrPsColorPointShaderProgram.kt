package com.mr.mf_pd.application.opengl.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

class PrPsColorPointShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.simple_vertex_shader_1,
    R.raw.simple_fragment_shader_1
) {
    private val uMatrixLocation: Int = GLES30.glGetUniformLocation(program, U_MATRIX)
    val aPositionLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
    val aColorLocation: Int = GLES30.glGetAttribLocation(program, A_COLOR)


    fun setUniforms(matrix: FloatArray?) {
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }


}