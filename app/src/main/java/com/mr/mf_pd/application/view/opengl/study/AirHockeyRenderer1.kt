package com.mr.mf_pd.application.view.opengl.study

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.utils.ResReadUtils
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

class AirHockeyRenderer1 : GLSurfaceView.Renderer {

    private var vertexData: FloatBuffer
    private var aColorLocation = 0
    private var aPositionLocation = 0


    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
        private const val COLOR_COMPONENT_COUNT = 3
        private const val A_COLOR = "a_Color"
        private const val A_POSITION = "a_Position"
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    init {
        val tableVertices = floatArrayOf(
            //三角
            0f, 0f, 1f, 1f, 1f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            //线
            -0.5f, 0f, 1.0f, 0f, 0f,
            0.5f, 0f, 1.0f, 0f, 0f,
            //
            0f, -0.25f, 0f, 1f, 0f,
            0f, 0.2f, 0f, 0f, 1f
        )
        vertexData = ByteBuffer.allocateDirect(tableVertices.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData.put(tableVertices)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("za", "onSurfaceCreated")
        GLES30.glClearColor(0f, 0f, 0f, 0f)

        val vertexShaderSource = ResReadUtils.readResource(R.raw.simple_vertex_shader)
        val fragmentShaderSource = ResReadUtils.readResource(R.raw.simple_fragment_shader)

        val vertexShader = ShaderUtils.compileVertexShader(vertexShaderSource)
        val fragmentShader = ShaderUtils.compileFragmentShader(fragmentShaderSource)
        val program: Int = ShaderUtils.linkProgram(vertexShader, fragmentShader)
        GLES30.glUseProgram(program)
        ShaderUtils.validProgram(program)

        aColorLocation = GLES30.glGetAttribLocation(program, A_COLOR)
        aPositionLocation = GLES30.glGetAttribLocation(program, A_POSITION)

        vertexData.position(0)
        GLES30.glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GLES30.GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        GLES30.glEnableVertexAttribArray(aPositionLocation)

        vertexData.position(POSITION_COMPONENT_COUNT)
        GLES30.glVertexAttribPointer(
            aColorLocation, COLOR_COMPONENT_COUNT, GLES30.GL_FLOAT, false,
            STRIDE, vertexData
        )
        GLES30.glEnableVertexAttribArray(aColorLocation)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("za", "onSurfaceChanged")
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("za", "onDrawFrame")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 6)

        GLES30.glDrawArrays(GLES30.GL_LINES, 6, 2)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 8, 1)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 9, 1)
    }
}