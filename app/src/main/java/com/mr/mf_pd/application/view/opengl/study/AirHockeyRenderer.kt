package com.mr.mf_pd.application.view.opengl.study

import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer : GLSurfaceView.Renderer {

    val BYTES_PER_FLOAT = 4
   lateinit var vertexBuffer: FloatBuffer

    constructor(){
        var tableVertices  =floatArrayOf(
            //第一个三角
            0f, 0f,
            0f, 14f,
            9f, 14f,
            //第二个三角
            0f, 0f,
            9f, 14f,
            9f, 0f
        )
        vertexBuffer = ByteBuffer.allocateDirect(tableVertices.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(tableVertices)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(gl: GL10?) {
        TODO("Not yet implemented")
    }
}