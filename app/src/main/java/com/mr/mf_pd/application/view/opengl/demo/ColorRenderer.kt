package com.mr.mf_pd.application.view.opengl.demo

import android.graphics.Color
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.R
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ColorRenderer(var color:Int) : GLSurfaceView.Renderer {


    private var mProgram = 0

    private val POSITION_COMPONENT_COUNT = 3

    private val vertexPoints = floatArrayOf(
        0.25f, 0.25f, 0.0f,  //V0
        -0.75f, 0.25f, 0.0f,  //V1
        -0.75f, -0.75f, 0.0f,  //V2
        0.25f, -0.75f, 0.0f,  //V3
        0.75f, -0.25f, 0.0f,  //V4
        0.75f, 0.75f, 0.0f,  //V5
        -0.25f, 0.75f, 0.0f,  //V6
        -0.25f, -0.25f, 0.0f,  //V7
        -0.25f, 0.75f, 0.0f,  //V6
        -0.75f, 0.25f, 0.0f,  //V1
        0.75f, 0.75f, 0.0f,  //V5
        0.25f, 0.25f, 0.0f,  //V0
        -0.25f, -0.25f, 0.0f,  //V7
        -0.75f, -0.75f, 0.0f,  //V2
        0.75f, -0.25f, 0.0f,  //V4
        0.25f, -0.75f, 0.0f //V3
    )


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        //设置视图窗口
        val redF = Color.red(color).toFloat() / 255
        val greenF = Color.green(color).toFloat() / 255
        val blueF = Color.blue(color).toFloat() / 255
        val alphaF = Color.alpha(color).toFloat() / 255
        GLES30.glClearColor(redF, greenF, blueF, alphaF)

        //设置背景颜色

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        //设置背景颜色
        GLES30.glViewport(0, 0, width, height);
    }

    override fun onDrawFrame(p0: GL10?) {
        //把颜色缓冲区设置为我们预设的颜色
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT)

    }




}