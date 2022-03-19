package com.mr.mf_pd.application.view.opengl.`object`

import android.opengl.GLES30
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.data.VertexArray
import com.mr.mf_pd.application.view.opengl.programs.TextureShader3DProgram

open class TextGlPrpsHelp {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
        private const val TEXTURE_COORDINATES_COUNT = 2
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COUNT) * Constants.BYTES_PER_FLOAT

        private val VERTEX_DATA_XY = floatArrayOf(
            //X Y Z S T
            -1f, -1f, 0f, 0f, 1.0f,
            1f, -1f, 0f, 1.0f, 1.0f,
            -1f, 1f, 0f, 0f, 0f,
            1f, 1f, 0f, 1.0f, 0f,
        )

        private val VERTEX_DATA_XZ = floatArrayOf(
            //X Y Z S T
            -1f, 0.85f, 0f, 0f, 1.0f,
            1f, 0.85f, 0f, 1.0f, 1.0f,
            -1f, 0.85f, 1.7f, 0f, 0f,
            1f, 0.85f, 1.7f, 1.0f, 0f,
        )
    }

    private lateinit var vertexArray: VertexArray

    fun bindData(textureProgram: TextureShader3DProgram) {

        vertexArray = VertexArray(VERTEX_DATA_XY)

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

    fun bindXZData(textureProgram: TextureShader3DProgram) {

        vertexArray = VertexArray(VERTEX_DATA_XZ)

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

    fun bindData(textureProgram: TextureShader3DProgram, floatArray: FloatArray) {
        vertexArray = VertexArray(floatArray)
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
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

}