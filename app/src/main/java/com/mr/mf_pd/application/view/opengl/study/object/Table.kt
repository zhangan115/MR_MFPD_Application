package com.mr.mf_pd.application.view.opengl.study.`object`

import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.study.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.study.data.VertexArray

class Table {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COUNT = 2
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            //三角
            0f, 0f, 0.5f, 0.5f,
            -0.5f, -0.8f, 0f, 0.9f,
            0.5f, -0.8f, 01f, 0.9f,
            0.5f, 0.8f, 1f, 0.1f,
            -0.5f, 0.8f, 0f, 0.1f,
            -0.5f, -0.8f, 0f, 0.9f,
        )
    }

    private val vertexArray:VertexArray

    init {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    public fun bindData(textureProgram: TextureShaderProgram){

    }

}