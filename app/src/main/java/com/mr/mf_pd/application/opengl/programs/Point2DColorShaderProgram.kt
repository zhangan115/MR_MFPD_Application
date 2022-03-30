package com.mr.mf_pd.application.opengl.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

class Point2DColorShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.point2d_vertex_shader,
    R.raw.point2d_fragment_shader
) {
    val aPositionLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
    private val uColorLocation: Int = GLES30.glGetUniformLocation(program, U_COLOR)

    fun setUniforms(r: Float, g: Float, b: Float) {
        GLES30.glUniform4f(uColorLocation, r, g, b, 1f)
    }

}