package com.mr.mf_pd.application.view.opengl.study

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.utils.ResReadUtils.readResource
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils.compileFragmentShader
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils.compileVertexShader
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils.linkProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LineRenderer : GLSurfaceView.Renderer {
    private var mProgram = 0
    override fun onSurfaceCreated(gl10: GL10, config: EGLConfig) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val vertexShaderId = compileVertexShader(readResource(R.raw.vertex_shader_point_1))
        val fragmentShaderId = compileFragmentShader(readResource(R.raw.fragment_shader_point_1))
        //链接程序片段
        mProgram = linkProgram(vertexShaderId, fragmentShaderId)
        //使用程序片段
        GLES30.glUseProgram(mProgram)
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl10: GL10) {
        val random = Math.random().toFloat()
        val pointVFA = floatArrayOf(
            0.5f, 0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
        )
        val vertexBuffer = ByteBuffer.allocateDirect(pointVFA.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(pointVFA)
        vertexBuffer.position(0)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, 3)
        GLES30.glLineWidth(20f)

//        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, pointVFA.length / 3);
        GLES30.glDisableVertexAttribArray(0)
    }
}