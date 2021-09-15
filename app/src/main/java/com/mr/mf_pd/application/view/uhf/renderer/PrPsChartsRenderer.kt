package com.mr.mf_pd.application.view.uhf.renderer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.`object`.PrPs3DArea
import com.mr.mf_pd.application.view.opengl.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PrPsChartsRenderer(var context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)


    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private var texture: Int = 0

    private lateinit var prPs3DArea: PrPs3DArea

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(1f, 1f, 1f, 1f)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        prPs3DArea = PrPs3DArea(4)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        MatrixUtils.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 1.6f, 2.6f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

//        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
//        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)

        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.5f, 0.5f, 0.5f)
        prPs3DArea.bindData(colorProgram)
        prPs3DArea.draw()
    }
}