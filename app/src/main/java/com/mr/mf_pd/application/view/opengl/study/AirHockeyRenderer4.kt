package com.mr.mf_pd.application.view.opengl.study

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.ResReadUtils
import com.mr.mf_pd.application.view.opengl.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 第七章 使用纹理
 */
class AirHockeyRenderer4 : GLSurfaceView.Renderer {

    private var vertexData: FloatBuffer
    private var aColorLocation = 0
    private var aPositionLocation = 0
    private var uMatrixLocation = 0
    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
        private const val COLOR_COMPONENT_COUNT = 3
        private const val A_COLOR = "a_Color"
        private const val A_POSITION = "a_Position"
        private const val U_MATRIX = "u_Matrix"
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
        uMatrixLocation = GLES30.glGetUniformLocation(program, U_MATRIX)

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
//        val aspectRatio =
//            if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
//        Log.d("za",aspectRatio.toString())
//        if (width > height) {
//            //横屏
//            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
//        } else {
//            //竖屏
//            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
//        }
        MatrixUtils.perspectiveM(
            projectionMatrix, 45f,
            (width.toFloat() / height.toFloat()), 1f, 10f
        )
        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.translateM(modelMatrix, 0, 0f, 0f, -2f)

        Matrix.translateM(modelMatrix, 0,  0f, 0f, -2.5f)
        Matrix.rotateM(modelMatrix,0,-60f,1f,0f,0f)

        val temp = FloatArray(16)
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)


    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("za", "onDrawFrame")
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 6)

        GLES30.glDrawArrays(GLES30.GL_LINES, 6, 2)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 8, 1)

        GLES30.glDrawArrays(GLES30.GL_POINTS, 9, 1)
    }
}