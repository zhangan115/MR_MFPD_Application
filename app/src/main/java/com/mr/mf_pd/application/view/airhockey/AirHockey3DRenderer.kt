package com.mr.mf_pd.application.view.airhockey

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.airhockey.`object`.Mallet
import com.mr.mf_pd.application.view.airhockey.`object`.Puck
import com.mr.mf_pd.application.view.airhockey.`object`.Table
import com.mr.mf_pd.application.view.airhockey.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.airhockey.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.airhockey.utils.MatrixUtils
import com.mr.mf_pd.application.view.airhockey.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * 空气球
 */
class AirHockey3DRenderer(var context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private var table: Table = Table()
    private var puck: Puck
    private var mallet: Mallet

    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private var texture: Int = 0

    init {
        puck = Puck(0.06f, 0.02f, 128)
        mallet = Mallet(0.08f, 0.15f, 128)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("za", "onSurfaceCreated")
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1f)

        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        texture = TextureUtils.loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("za", "onSurfaceChanged")
        GLES30.glViewport(0, 0, width, height)
        MatrixUtils.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.6f, 2.6f, 0f, 0f, 0f, 0f, 1f, 0f)

    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("za", "onDrawFrame")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        positionTableInScene()
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix, texture)
        table.bindData(textureProgram)
        table.draw()

        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorProgram.useProgram()
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet.bindData(colorProgram)
        mallet.draw()

        positionObjectInScene(0f, mallet.height / 2f, 0.4f)
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 1f, 0f)
        mallet.draw()

        positionObjectInScene(0f, puck.height / 2f, 0f)
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 0f)
        puck.bindData(colorProgram)
        puck.draw()
    }

    private fun positionTableInScene() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }

    private fun positionObjectInScene(x: Float, y: Float, z: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0)
    }


}