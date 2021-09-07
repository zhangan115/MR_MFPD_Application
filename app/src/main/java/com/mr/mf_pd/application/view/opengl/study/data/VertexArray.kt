/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.mr.mf_pd.application.view.opengl.study.data

import android.opengl.GLES30
import com.mr.mf_pd.application.common.Constants
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VertexArray(vertexData: FloatArray) {

    /**
     * 存储本地定点矩阵数据
     */
    private val floatBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertexData.size * Constants.BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertexData)

    /**
     * 设置定点属性
     */
    fun setVertexAttributePointer(
        dataOffset: Int, attributeLocation: Int,
        componentCount: Int, stride: Int
    ) {
        floatBuffer.position(dataOffset)
        GLES30.glVertexAttribPointer(
            attributeLocation, componentCount, GLES30.GL_FLOAT,
            false, stride, floatBuffer
        )
        GLES30.glEnableVertexAttribArray(attributeLocation)
        floatBuffer.position(0)
    }

}