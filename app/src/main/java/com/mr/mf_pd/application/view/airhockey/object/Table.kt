package com.mr.mf_pd.application.view.airhockey.`object`

import android.opengl.GLES30
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.airhockey.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.airhockey.data.VertexArray

open class Table {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
        private const val TEXTURE_COORDINATES_COUNT = 2
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            //X Y Z S T
            0f, 0f,0f, 0.5f, 0.5f,
            -0.8f, -0.8f,0f, 0f, 0.9f,
            0.8f, -0.8f, 0f,1f, 0.9f,
            0.8f, 0.8f, 0f,1f, 0.1f,
            -0.8f, 0.8f, 0f,0f, 0.1f,
            -0.8f, -0.8f, 0f,0f, 0.9f,
        )
    }

    private val vertexArray: VertexArray

    init {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    fun bindData(textureProgram: TextureShaderProgram) {
        vertexArray.setVertexAttributePointer(
            0,
            textureProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT, STRIDE
        )
        vertexArray.setVertexAttributePointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.textureCoordinatesAttributeLocation,
            TEXTURE_COORDINATES_COUNT,
            STRIDE
        )
    }

    fun draw() {
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 6)
    }

}