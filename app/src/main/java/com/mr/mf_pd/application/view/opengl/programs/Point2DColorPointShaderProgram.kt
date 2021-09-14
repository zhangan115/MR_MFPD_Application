package com.mr.mf_pd.application.view.opengl.programs

import android.content.Context
import android.opengl.GLES30
import com.mr.mf_pd.application.R

class Point2DColorPointShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.point2d_vertex_shader_1,
    R.raw.point2d_fragment_shader_1
) {
    val aPositionLocation: Int = GLES30.glGetAttribLocation(program, A_POSITION)
    val aColorLocation: Int = GLES30.glGetAttribLocation(program, A_COLOR)


}