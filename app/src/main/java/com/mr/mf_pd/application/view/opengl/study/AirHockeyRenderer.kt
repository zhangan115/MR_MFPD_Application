package com.mr.mf_pd.application.view.opengl.study

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer : GLSurfaceView.Renderer {

   private val BYTES_PER_FLOAT = 4
   private lateinit var vertexBuffer: FloatBuffer

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
        Log.d("za", "onSurfaceCreated")
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("za", "onSurfaceChanged")
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("za", "onDrawFrame")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }
}