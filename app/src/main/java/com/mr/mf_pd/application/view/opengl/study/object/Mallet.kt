package com.mr.mf_pd.application.view.opengl.study.`object`

import android.opengl.GLES30
import com.mr.mf_pd.application.common.Constants.BYTES_PER_FLOAT
import com.mr.mf_pd.application.view.opengl.study.data.VertexArray
import com.mr.mf_pd.application.view.opengl.study.programs.ColorShaderProgram

class Mallet {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val COLOR_COMPONENT_COUNT = 3
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            //X Y S T
            0f, 0.4f, 1f, 0f, 0f,
            0f, -0.4f, 0f, 01f, 0f,
        )
    }

    private val vertexArray: VertexArray

    init {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    fun bindData(colorShaderProgram: ColorShaderProgram) {
        vertexArray.setVertexAttributePointer(
            0, colorShaderProgram.aPositionLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        vertexArray.setVertexAttributePointer(
            POSITION_COMPONENT_COUNT, colorShaderProgram.aColorLocation,
            COLOR_COMPONENT_COUNT, STRIDE
        )
    }

    fun draw() {
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 2)
    }
}