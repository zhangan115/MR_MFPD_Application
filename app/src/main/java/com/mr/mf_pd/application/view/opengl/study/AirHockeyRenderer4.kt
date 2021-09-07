package com.mr.mf_pd.application.view.opengl.study

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.opengl.study.`object`.Mallet
import com.mr.mf_pd.application.view.opengl.study.`object`.Table
import com.mr.mf_pd.application.view.opengl.study.programs.ColorShaderProgram
import com.mr.mf_pd.application.view.opengl.study.programs.TextureShaderProgram
import com.mr.mf_pd.application.view.opengl.utils.MatrixUtils
import com.mr.mf_pd.application.view.opengl.utils.TextureUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 第七章 使用纹理
 */
class AirHockeyRenderer4(var context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private lateinit var table: Table
    private lateinit var mallet: Mallet
    private lateinit var textureProgram: TextureShaderProgram
    private lateinit var colorProgram: ColorShaderProgram
    private var texture: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("za", "onSurfaceCreated")
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        table = Table()
        mallet = Mallet()

        textureProgram =  TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)

        texture = TextureUtils.loadTexture(context, R.drawable.air_hockey_surface)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("za", "onSurfaceChanged")
        GLES30.glViewport(0, 0, width, height)
        MatrixUtils.perspectiveM(
            projectionMatrix, 45f,
            (width.toFloat() / height.toFloat()), 1f, 10f
        )
        Matrix.setIdentityM(modelMatrix, 0)

        Matrix.translateM(modelMatrix, 0, 0f, 0f, -3f)
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f)

        val temp = FloatArray(16)
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0)
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.size)


    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("za", "onDrawFrame")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        textureProgram.useProgram()
        textureProgram.setUniforms(projectionMatrix,texture)
        table.bindData(textureProgram)
        table.draw()

        colorProgram.useProgram()
        colorProgram.setUniforms(projectionMatrix)
        mallet.bindData(colorProgram)
        mallet.draw()
    }
}